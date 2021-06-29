package com.github.falsepattern.jblotter.objects.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.pegs.Input;
import com.github.falsepattern.jblotter.objects.component.pegs.Output;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;
import com.github.falsepattern.jblotter.util.json.Jsonifier;
import com.github.falsepattern.jblotter.util.json.rule.DynamicArrayRule;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.TextRule;
import com.github.falsepattern.jblotter.util.serialization.SerializationUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public record Component(int address, int parentAddress, short componentID, Vector3f localPosition, Quaternionf localRotation, Input[] inputs, Output[] outputs, byte[] customData) implements Serializable {
    public static final NodeRule RULE = new ObjectRule(new String[]{"componentAddress", "parentAddress", "componentID", "localPosition", "localRotation", "inputs", "outputs", "customData"},
            new NodeRule[]{IntegerRule.UNSIGNED_INT, IntegerRule.UNSIGNED_INT, IntegerRule.UNSIGNED_SHORT, ObjectRule.RULE_VEC3, ObjectRule.RULE_QUATERNION, new DynamicArrayRule(Input.RULE), new DynamicArrayRule(Output.RULE), new DynamicArrayRule(IntegerRule.UNSIGNED_BYTE)}, true);
    public static final NodeRule EDITABLE_RULE = new ObjectRule(new String[]{"componentAddress", "parentAddress", "componentID", "localPosition", "localRotation", "inputs", "outputs", "customData"},
            new NodeRule[]{IntegerRule.UNSIGNED_INT, IntegerRule.UNSIGNED_INT, TextRule.INSTANCE, ObjectRule.RULE_VEC3, ObjectRule.RULE_QUATERNION, new DynamicArrayRule(Input.EDITABLE_RULE), new DynamicArrayRule(Output.EDITABLE_RULE), new DynamicArrayRule(IntegerRule.UNSIGNED_BYTE)}, true);
    public static Component deserialize(DataInput input, Component[] components) throws IOException {
        var address = input.readInt();
        var parentAddress = input.readInt();
        if (parentAddress != 0 && components[parentAddress] == null) throw new IllegalArgumentException("Parent component with ID " + parentAddress + " not found!");
        var componentID = input.readShort();
        var position = SerializationUtil.deserializeVector3f(input);
        var rotation = SerializationUtil.deserializeQuaternionf(input);
        var inputs = new Input[input.readUnsignedByte()];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = Input.deserialize(input);
        }
        var outputs = new Output[input.readUnsignedByte()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = Output.deserialize(input);
        }
        var customData = new byte[input.readInt()];
        input.readFully(customData);
        return new Component(address, parentAddress, componentID, position, rotation, inputs, outputs, customData);
    }

    public static Component fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) RULE.verify(node);
        return new Component((int)node.get("componentAddress").longValue(), (int)node.get("parentAddress").longValue(), (short) node.get("componentID").intValue(),
                JsonUtil.parseVector(node.get("localPosition"), true), JsonUtil.parseQuaternion(node.get("localRotation"), true),
                JsonUtil.parseArrayNoVerify(node.get("inputs"), 0, 0, Input[]::new, (input) -> Input.fromJson(input, true)),
                JsonUtil.parseArrayNoVerify(node.get("outputs"), 0, 0, Output[]::new, (output) -> Output.fromJson(output, true)),
                JsonUtil.parseByteArray(node.get("customData"), true));
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(address);
        output.writeInt(parentAddress);
        output.writeShort(componentID);
        SerializationUtil.serializeVector3f(output, localPosition);
        SerializationUtil.serializeQuaternionf(output, localRotation);
        output.writeByte(inputs.length);
        for (var Input: inputs) {
            Input.serialize(output);
        }
        output.writeByte(outputs.length);
        for (var Output: outputs) {
            Output.serialize(output);
        }
        output.writeInt(customData.length);
        output.write(customData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return address == component.address && parentAddress == component.parentAddress && componentID == component.componentID && localPosition.equals(component.localPosition) && localRotation.equals(component.localRotation) && Arrays.equals(inputs, component.inputs) && Arrays.equals(outputs, component.outputs) && Arrays.equals(customData, component.customData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(address, parentAddress, componentID, localPosition, localRotation);
        result = 31 * result + Arrays.hashCode(inputs);
        result = 31 * result + Arrays.hashCode(outputs);
        result = 31 * result + Arrays.hashCode(customData);
        return result;
    }

    private ObjectNode toJson(String name, Jsonifier<Input> inputMethod, Jsonifier<Output> outputMethod) {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("componentAddress", Integer.toUnsignedLong(address));
        result.put("parentAddress", Integer.toUnsignedLong(parentAddress));
        if (name != null) {
            result.put("componentID", name);
        } else {
            result.put("componentID", componentID);
        }
        result.set("localPosition", JsonUtil.jsonifyVector3f(localPosition));
        result.set("localRotation", JsonUtil.jsonifyQuaternionf(localRotation));
        result.set("inputs", JsonUtil.jsonifyArray(inputs, inputMethod));
        result.set("outputs", JsonUtil.jsonifyArray(outputs, outputMethod));
        result.set("customData", JsonUtil.jsonifyByteArray(customData));
        return result;
    }

    @Override
    public ObjectNode toJson() {
        return toJson(null, Serializable::toJson, Serializable::toJson);
    }

    public ObjectNode toEditableJson(BitSet circuitStates, String[] ids) {
        return toJson(ids[componentID], Input::toEditableJson, (output) -> output.toEditableJson(circuitStates));
    }

    @Override
    public ObjectNode toEditableJson() {
        throw new UnsupportedOperationException("Use toEditableJson(BitSet, String[]) for serializing components! They require context about circuit states and component IDs!");
    }
}
