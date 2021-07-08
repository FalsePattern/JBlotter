package com.github.falsepattern.jblotter.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.Component;
import com.github.falsepattern.jblotter.objects.component.Wire;
import com.github.falsepattern.jblotter.util.SignalGraphSolver;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;
import com.github.falsepattern.jblotter.util.json.rule.DynamicArrayRule;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.BooleanRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.TextRule;
import com.github.falsepattern.jblotter.util.serialization.SerializationUtil;
import org.w3c.dom.Node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The deserialized version of the Blotter File Format<br>
 * Note that the {@link #components} array's first element is always null, and it's 1 element larger than the component
 * count. This is so that component addresses can be used to directly address the array without the need of lookup logic.
 */
public record BlotterFile(byte saveFormatVersion, Version gameVersion, Map<String, Version> mods, boolean isWorld, String[] componentIDs, Map<Integer, Component> components, Wire[] wires, int circuitStateCount, BitSet worldCircuitStates, int[] subassemblyCircuitStates) implements Serializable {
    private static final byte[] DESIRED_HEADER = new byte[]{0x4C, 0x6F, 0x67, 0x69, 0x63, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x20, 0x73, 0x61, 0x76, 0x65};
    private static final byte[] DESIRED_FOOTER = new byte[]{0x72, 0x65, 0x64, 0x73, 0x74, 0x6F, 0x6E, 0x65, 0x20, 0x73, 0x75, 0x78, 0x20, 0x6C, 0x6F, 0x6C};
    private static final ObjectRule COMMON_RULE = new ObjectRule(new String[]{"saveFormatVersion", "gameVersion", "mods", "saveType"}, new NodeRule[]{IntegerRule.UNSIGNED_BYTE, Version.RULE, new DynamicArrayRule(new ObjectRule(new String[]{"name", "version"}, new NodeRule[]{TextRule.INSTANCE, Version.RULE}, true)), IntegerRule.UNSIGNED_BYTE}, false);
    public static final NodeRule RULE_BASE = ObjectRule.join(COMMON_RULE, new ObjectRule(new String[]{"componentIDs", "components", "wires"}, new NodeRule[]{new DynamicArrayRule(TextRule.INSTANCE), new DynamicArrayRule(Component.RULE), new DynamicArrayRule(Wire.RULE)}, false), false);
    public static final NodeRule RULE_WORLD = new ObjectRule(new String[]{"circuitStates"}, new NodeRule[]{new DynamicArrayRule(BooleanRule.INSTANCE)}, false);
    public static final NodeRule RULE_SUBASSEMBLY = new ObjectRule(new String[]{"circuitStates"}, new NodeRule[]{new DynamicArrayRule(IntegerRule.POSITIVE_SIGNED_INT)}, false);
    public static final NodeRule EDITABLE_RULE = ObjectRule.join(COMMON_RULE, new ObjectRule(new String[]{"components", "wires", "editFriendly"}, new NodeRule[]{new DynamicArrayRule(Component.EDITABLE_RULE), new DynamicArrayRule(Wire.EDITABLE_RULE), BooleanRule.INSTANCE}, false), true);

    public Component getComponentByID(int id) {
        return components.get(id);
    }

    public int getComponentCount() {
        return components.size();
    }

    public static BlotterFile deserialize(DataInput input) throws IOException {
        if (input == null) throw new NullPointerException("Cannot deserialize from null stream!");
        {
            var header = new byte[DESIRED_HEADER.length];
            input.readFully(header);
            if (!Arrays.equals(DESIRED_HEADER, header)) {
                throw new IllegalArgumentException("Save file header mismatch!");
            }
        }

        var saveFormatVersion = input.readByte();
        var gameVersion = Version.deserialize(input);
        var isWorld = readWorldBoolean(input);
        var componentCount = input.readInt();
        var wireCount = input.readInt();
        var mods = readModVersions(input);
        var componentIDs = readComponentIDMap(input);
        var components = readComponents(input, componentCount);
        var wires = readWires(input, wireCount);
        BitSet worldCircuitStates = null;
        int[] subassemblyCircuitStates = null;
        int circuitStateCount;
        if (isWorld) {
            var rawState = new byte[input.readInt()];
            circuitStateCount = rawState.length * 8;
            input.readFully(rawState);
            worldCircuitStates = BitSet.valueOf(rawState);
        } else {
            subassemblyCircuitStates = new int[input.readInt()];
            circuitStateCount = subassemblyCircuitStates.length;
            for (int i = 0; i < subassemblyCircuitStates.length; i++) {
                subassemblyCircuitStates[i] = input.readInt();
            }
        }
        {
            var footer = new byte[DESIRED_FOOTER.length];
            input.readFully(footer);
            if (!Arrays.equals(DESIRED_FOOTER, footer)) {
                throw new IllegalArgumentException("Save file footer mismatch!");
            }
        }
        return new BlotterFile(saveFormatVersion, gameVersion, mods, isWorld, componentIDs, components, wires, circuitStateCount, worldCircuitStates, subassemblyCircuitStates);
    }

    private static BlotterFile fromStandardJson(JsonNode node) throws JsonParseException {
        var saveFormatVersion = (byte)node.get("saveFormatVersion").intValue();
        var gameVersion = Version.fromJson(node.get("gameVersion"), true);
        var mods = new HashMap<String, Version>();
        for (JsonNode entry : node.get("mods")) {
            mods.put(entry.get("name").textValue(), Version.fromJson(entry.get("version"), true));
        }
        var isWorld = switch (node.get("saveType").intValue()) {
            default -> throw new JsonParseException("Unknown/corrupted save type!");
            case 0x01 -> true;
            case 0x02 -> false;
        };
        var componentIDs = JsonUtil.parseArrayNoVerify(node.get("componentIDs"), 0, 0, String[]::new, JsonNode::textValue);
        var components = new HashMap<Integer, Component>();
        for (var componentNode: node.get("components")) {
            var component = Component.fromJson(componentNode, false);
            if (components.containsKey(component.address())) throw new IllegalArgumentException("Component address conflict: " + component.address());
            components.put(component.address(), component);
        }
        var wires = JsonUtil.parseArrayNoVerify(node.get("wires"), 0, 0, Wire[]::new, (wire) -> Wire.fromJson(wire, true));
        var stateArray = node.get("circuitStates");
        int stateArraySize = stateArray.size();
        BitSet worldStates = null;
        int[] subassemblyStates = null;
        if (isWorld) {
            RULE_WORLD.verify(node);
            worldStates = new BitSet(stateArraySize);
            for (int i = 0; i < stateArraySize; i++) {
                worldStates.set(i, stateArray.get(i).asBoolean());
            }
        } else {
            RULE_SUBASSEMBLY.verify(node);
            subassemblyStates = new int[stateArraySize];
            for (int i = 0; i < stateArraySize; i++) {
                subassemblyStates[i] = stateArray.get(i).intValue();
            }
        }
        return new BlotterFile(saveFormatVersion, gameVersion, mods, isWorld, componentIDs, components, wires, stateArraySize, worldStates, subassemblyStates);
    }

    public static BlotterFile fromJson(JsonNode node) throws JsonParseException {
        COMMON_RULE.verify(node);
        var saveFormatVersion = node.get("saveFormatVersion").intValue();
        if (saveFormatVersion != 0x05) {
            throw new JsonParseException("Unsupported save format version " + saveFormatVersion);
        }
        var isWorld = switch (node.get("saveType").intValue()) {
            default -> throw new JsonParseException("Unknown/corrupted save type!");
            case 0x01 -> true;
            case 0x02 -> false;
        };
        boolean editFriendly = false;
        if (node.has("editFriendly")) {
            var nef = node.get("editFriendly");
            BooleanRule.INSTANCE.verify(nef);
            editFriendly = nef.asBoolean();
        }
        if (editFriendly) {
            EDITABLE_RULE.verify(node);
            node = node.deepCopy();
            var solver = new SignalGraphSolver();
            solver.addComponents((ArrayNode) node.get("components"));
            solver.addWires((ArrayNode) node.get("wires"));
            solver.solve((ObjectNode) node, isWorld);
            var componentIDs = new ArrayList<String>();
            for (var component: node.get("components")) {
                var name = component.get("componentID").textValue();
                if (!componentIDs.contains(name)) {
                    componentIDs.add(name);
                }
                ((ObjectNode)component).put("componentID", componentIDs.indexOf(name));
            }
            var compIdArr = new ArrayNode(JsonNodeFactory.instance);
            for (var id: componentIDs) {
                compIdArr.add(id);
            }
            ((ObjectNode) node).set("componentIDs", compIdArr);
        }
        RULE_BASE.verify(node);
        return fromStandardJson(node);

    }

    public void serialize(DataOutput output) throws IOException {
        output.write(DESIRED_HEADER);
        output.writeByte(saveFormatVersion);
        gameVersion.serialize(output);
        output.writeByte(isWorld ? 0x01 : 0x02);
        output.writeInt(components.size());
        output.writeInt(wires.length);
        output.writeInt(mods.size());
        for (var mod: mods.entrySet()) {
            SerializationUtil.serializeString(output, mod.getKey());
            mod.getValue().serialize(output);
        }
        writeComponentIDMap(output);
        for (var component: components.values()) {
            component.serialize(output);
        }
        for (Wire wire : wires) {
            wire.serialize(output);
        }
        if (isWorld) {
            int stateByteCount = (int)Math.ceil(circuitStateCount / 8d);
            output.writeInt(stateByteCount);
            var states = worldCircuitStates.toByteArray();
            output.write(states);
            for (int i = states.length; i < stateByteCount; i++) {
                output.writeByte(0);
            }
        } else {
            output.writeInt(subassemblyCircuitStates.length);
            for (int state: subassemblyCircuitStates) {
                output.writeInt(state);
            }
        }
        output.write(DESIRED_FOOTER);
    }

    private ObjectNode toJsonPrelude() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("saveFormatVersion", saveFormatVersion);
        result.set("gameVersion", gameVersion.toJson());
        result.put("saveType", isWorld ? 1 : 2);
        var modsNode = new ArrayNode(JsonNodeFactory.instance);
        for (var mod: mods.entrySet()) {
            var modNode = new ObjectNode(JsonNodeFactory.instance);
            modNode.put("name", mod.getKey());
            modNode.set("version", mod.getValue().toJson());
            modsNode.add(modNode);
        }
        result.set("mods", modsNode);
        return result;
    }

    @Override
    public ObjectNode toJson() {
        var result = toJsonPrelude();
        result.set("componentIDs", JsonUtil.jsonifyArray(componentIDs, TextNode::new));
        var comps = new ArrayNode(JsonNodeFactory.instance);
        for (var component: components.values()) {
            var comp = component.toJson();
            comps.add(comp);
        }
        result.set("components", comps);
        result.set("wires", JsonUtil.jsonifyArray(wires, Wire::toJson));
        if (isWorld) {
            var stateArray = new ArrayNode(JsonNodeFactory.instance);
            for (int i = 0; i < circuitStateCount; i++) {
                stateArray.add(worldCircuitStates.get(i));
            }
            result.set("circuitStates", stateArray);
        } else {
            var stateArray = new ArrayNode(JsonNodeFactory.instance);
            for (int i = 0; i < circuitStateCount; i++) {
                stateArray.add(subassemblyCircuitStates[i]);
            }
            result.set("circuitStates", stateArray);
        }
        result.put("editFriendly", false);
        return result;
    }

    @Override
    public ObjectNode toEditableJson() {
        var result = toJsonPrelude();
        BitSet circuitStates;
        if (isWorld) {
            circuitStates = worldCircuitStates;
        } else {
            circuitStates = new BitSet();
            for (int subassemblyCircuitState : subassemblyCircuitStates) {
                circuitStates.set(subassemblyCircuitState);
            }
        }
        var comps = new ArrayNode(JsonNodeFactory.instance);
        for (var component: components.values()) {
            var comp = component.toEditableJson(circuitStates, componentIDs);
            comps.add(comp);
        }
        result.set("components", comps);
        result.set("wires", JsonUtil.jsonifyArray(wires, Wire::toEditableJson));
        result.put("editFriendly", true);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlotterFile that = (BlotterFile) o;
        return saveFormatVersion == that.saveFormatVersion && isWorld == that.isWorld && gameVersion.equals(that.gameVersion) && Arrays.equals(componentIDs, that.componentIDs) && Objects.equals(components, that.components) && Arrays.equals(wires, that.wires) && Objects.equals(worldCircuitStates, that.worldCircuitStates) && Arrays.equals(subassemblyCircuitStates, that.subassemblyCircuitStates);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(saveFormatVersion, gameVersion, isWorld, worldCircuitStates, components);
        result = 31 * result + Arrays.hashCode(componentIDs);
        result = 31 * result + Arrays.hashCode(wires);
        result = 31 * result + Arrays.hashCode(subassemblyCircuitStates);
        return result;
    }

    private static Map<String, Version> readModVersions(DataInput input) throws IOException {
        int modCount = input.readInt();
        var modMap = new HashMap<String, Version>();
        for (int i = 0; i < modCount; i++) {
            var name = SerializationUtil.deserializeString(input);
            var version = Version.deserialize(input);
            modMap.put(name, version);
        }
        return modMap;
    }

    private static boolean readWorldBoolean(DataInput input) throws IOException {
        return switch (input.readByte()) {
            default -> throw new IllegalArgumentException("Unknown/corrupted save type!");
            case 0x01 -> true;
            case 0x02 -> false;
        };
    }

    private static String[] readComponentIDMap(DataInput input) throws IOException {
        String[] componentIDMap = new String[input.readInt()];
        for (int i = 0; i < componentIDMap.length; i++) {
            var componentID = input.readUnsignedShort();
            if (componentIDMap[componentID] != null) throw new IllegalStateException("Component id conflict. This is unspecified behaviour, so the deserializer will now fail.");
            var componentName = SerializationUtil.deserializeString(input);
            componentIDMap[componentID] = componentName;
        }
        return componentIDMap;
    }

    private static Map<Integer, Component> readComponents(DataInput input, int componentCount) throws IOException {
        var components = new HashMap<Integer, Component>();
        for (int i = 1; i < componentCount + 1; i++) {
            var component = Component.deserialize(input, components);
            if (components.containsKey(component.address())) throw new IllegalStateException("Component address conflict. This is unspecified behaviour, so the deserializer will now fail.");
            components.put(component.address(), component);
        }
        return components;
    }

    private static Wire[] readWires(DataInput input, int wireCount) throws IOException {
        var wires = new Wire[wireCount];
        for (int i = 0; i < wireCount; i++) {
            wires[i] = Wire.deserialize(input);
        }
        return wires;
    }

    private void writeComponentIDMap(DataOutput output) throws IOException {
        output.writeInt(componentIDs.length);
        for (int i = 0; i < componentIDs.length; i++) {
            if (componentIDs[i] == null) throw new IllegalStateException("Component id " + i + " is a null string.");
            output.writeShort(i);
            SerializationUtil.serializeString(output, componentIDs[i]);
        }
    }

}
