package com.github.falsepattern.jblotter.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.Component;
import com.github.falsepattern.jblotter.objects.component.Wire;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Objects;

/**
 * The deserialized version of the Blotter File Format<br>
 * Note that the {@link #components} array's first element is always null, and it's 1 element larger than the component
 * count. This is so that component addresses can be used to directly address the array without the need of lookup logic.
 */
public final record BlotterFile(byte saveFormatVersion, GameVersion gameVersion, boolean isWorld, String[] componentIDs, Component[] components, Wire[] wires, BitSet worldCircuitStates, int[] subassemblyCircuitStates) implements Serializable {
    private static final byte[] DESIRED_HEADER = new byte[]{0x4C, 0x6F, 0x67, 0x69, 0x63, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x20, 0x73, 0x61, 0x76, 0x65};
    private static final byte[] DESIRED_FOOTER = new byte[]{0x72, 0x65, 0x64, 0x73, 0x74, 0x6F, 0x6E, 0x65, 0x20, 0x73, 0x75, 0x78, 0x20, 0x6C, 0x6F, 0x6C};

    public Component getComponentByID(int id) {
        return components[id];
    }

    public int getComponentCount() {
        return components.length - 1;
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
        var gameVersion = GameVersion.deserialize(input);
        var isWorld = readWorldBoolean(input);
        var componentCount = input.readInt();
        var wireCount = input.readInt();
        var componentIDs = readComponentIDMap(input);
        var components = readComponents(input, componentCount);
        var wires = readWires(input, wireCount);
        BitSet worldCircuitStates = null;
        int[] subassemblyCircuitStates = null;
        if (isWorld) {
            var rawState = new byte[input.readInt()];
            input.readFully(rawState);
            worldCircuitStates = BitSet.valueOf(rawState);
        } else {
            subassemblyCircuitStates = new int[input.readInt()];
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
        return new BlotterFile(saveFormatVersion, gameVersion, isWorld, componentIDs, components, wires, worldCircuitStates, subassemblyCircuitStates);
    }

    public static BlotterFile fromJson(JsonNode node) throws JsonParseException {
        JsonParseException verificationException = null;
        try {
            JsonUtil.verifyJsonObject(node, new String[]{"saveFormatVersion", "gameVersion", "saveType", "componentIDs", "components", "wires", "circuitStates"}, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.ARRAY, JsonNodeType.NUMBER, JsonNodeType.ARRAY, JsonNodeType.ARRAY, JsonNodeType.ARRAY, JsonNodeType.ARRAY});
        } catch (JsonParseException e) {
            verificationException = e;
        }
        var saveFormatVersion = JsonUtil.asUnsignedInteger(node.get("saveFormatVersion"), BigInteger.valueOf(0xff));
        if (saveFormatVersion.intValueExact() != 0x05) {
            if (verificationException == null) {
                throw new JsonParseException("Unsupported save format version " + saveFormatVersion.intValueExact());
            } else {
                throw new JsonParseException("Unsupported save format version " + saveFormatVersion.intValueExact(), verificationException);
            }
        } else if (verificationException != null) throw verificationException;
        var gameVersion = GameVersion.fromJson(node.get("gameVersion"));
        var isWorld = switch (JsonUtil.asUnsignedInteger(node.get("saveType"), BigInteger.valueOf(0x02)).intValueExact()) {
            default -> throw new JsonParseException("Unknown/corrupted save type!");
            case 0x01 -> true;
            case 0x02 -> false;
        };
        var componentIDs = JsonUtil.parseArray(node.get("componentIDs"), 0, 0, String[]::new, (node1) -> {
            if (!node1.isTextual()) throw new JsonParseException("Could not parse node as text:\n" + node1.toPrettyString());
            return node1.textValue();
        });
        var components = JsonUtil.parseArray(node.get("components"), 0, 1, Component[]::new, Component::fromJson);
        var wires = JsonUtil.parseArray(node.get("wires"), 0, 0, Wire[]::new, Wire::fromJson);
        var stateArray = node.get("circuitStates");
        JsonUtil.verifyDynamicLengthJsonArray(stateArray, JsonNodeType.NUMBER);
        int stateArraySize = stateArray.size();
        BitSet worldStates = null;
        int[] subassemblyStates = null;
        if (isWorld) {
            worldStates = new BitSet(stateArraySize);
            for (int i = 0; i < stateArraySize; i++) {
                var num = JsonUtil.asUnsignedInteger(stateArray.get(i), BigInteger.valueOf(0x01));
                worldStates.set(i, num.intValueExact() != 0);
            }
        } else {
            subassemblyStates = new int[stateArraySize];
            for (int i = 0; i < stateArraySize; i++) {
                var num = JsonUtil.asUnsignedInteger(stateArray.get(i), BigInteger.valueOf(Integer.MAX_VALUE));
                subassemblyStates[i] = num.intValueExact();
            }
        }
        return new BlotterFile((byte)saveFormatVersion.intValueExact(), gameVersion, isWorld, componentIDs, components, wires, worldStates, subassemblyStates);

    }

    public void serialize(DataOutput output) throws IOException {
        output.write(DESIRED_HEADER);
        output.writeByte(saveFormatVersion);
        gameVersion.serialize(output);
        output.writeByte(isWorld ? 0x01 : 0x02);
        output.writeInt(components.length - 1);
        output.writeInt(wires.length);
        writeComponentIDMap(output);
        for (int i = 1; i < components.length; i++) {
            components[i].serialize(output);
        }
        for (Wire wire : wires) {
            wire.serialize(output);
        }
        if (isWorld) {
            var states = worldCircuitStates.toByteArray();
            output.writeInt(states.length);
            output.write(states);
        } else {
            output.writeInt(subassemblyCircuitStates.length);
            for (int state: subassemblyCircuitStates) {
                output.writeInt(state);
            }
        }
        output.write(DESIRED_FOOTER);
    }

    @Override
    public ObjectNode toJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("saveFormatVersion", saveFormatVersion);
        result.set("gameVersion", gameVersion.toJson());
        result.put("saveType", isWorld ? 1 : 2);
        result.set("componentIDs", JsonUtil.jsonifyArray(componentIDs, TextNode::new));
        result.set("components", JsonUtil.jsonifyArray(components, 1, components.length, Component::toJson));
        result.set("wires", JsonUtil.jsonifyArray(wires, Wire::toJson));
        if (isWorld) {
            var stateArray = new ArrayNode(JsonNodeFactory.instance);
            int size = worldCircuitStates.length();
            for (int i = 0; i < size; i++) {
                stateArray.add(worldCircuitStates.get(i) ? 1 : 0);
            }
            result.set("circuitStates", stateArray);
        } else {
            var stateArray = new ArrayNode(JsonNodeFactory.instance);
            for (int subassemblyCircuitState : subassemblyCircuitStates) {
                stateArray.add(subassemblyCircuitState);
            }
            result.set("circuitStates", stateArray);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlotterFile that = (BlotterFile) o;
        return saveFormatVersion == that.saveFormatVersion && isWorld == that.isWorld && gameVersion.equals(that.gameVersion) && Arrays.equals(componentIDs, that.componentIDs) && Arrays.equals(components, that.components) && Arrays.equals(wires, that.wires) && Objects.equals(worldCircuitStates, that.worldCircuitStates) && Arrays.equals(subassemblyCircuitStates, that.subassemblyCircuitStates);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(saveFormatVersion, gameVersion, isWorld, worldCircuitStates);
        result = 31 * result + Arrays.hashCode(componentIDs);
        result = 31 * result + Arrays.hashCode(components);
        result = 31 * result + Arrays.hashCode(wires);
        result = 31 * result + Arrays.hashCode(subassemblyCircuitStates);
        return result;
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
        var componentNameBuffer = new byte[256];
        for (int i = 0; i < componentIDMap.length; i++) {
            var componentID = input.readUnsignedShort();
            if (componentIDMap[componentID] != null) throw new IllegalStateException("Component id conflict. This is unspecified behaviour, so the deserializer will now fail.");
            var componentNameBytes = input.readInt();
            if (componentNameBytes > componentNameBuffer.length) {
                componentNameBuffer = new byte[componentNameBytes];
            }
            input.readFully(componentNameBuffer, 0, componentNameBytes);
            var componentName = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(componentNameBuffer, 0, componentNameBytes)).toString();
            componentIDMap[componentID] = componentName;
        }
        return componentIDMap;
    }

    private static Component[] readComponents(DataInput input, int componentCount) throws IOException {
        var components = new Component[componentCount + 1];
        new HashMap<Integer, Component>();
        for (int i = 1; i < componentCount + 1; i++) {
            var component = Component.deserialize(input, components);
            if (component.address() > componentCount) {
                throw new IllegalStateException("Component address greater than component count. This is unspecified behaviour, so the deserializer will now fail.");
            }
            if (components[i] != null) throw new IllegalStateException("Component address conflict. This is unspecified behaviour, so the deserializer will now fail.");
            components[i] = component;
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
            var bytes = componentIDs[i].getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

}
