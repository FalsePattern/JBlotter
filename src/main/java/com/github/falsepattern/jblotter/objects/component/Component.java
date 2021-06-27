package com.github.falsepattern.jblotter.objects.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.pegs.Input;
import com.github.falsepattern.jblotter.objects.component.pegs.Output;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;
import com.github.falsepattern.jblotter.util.serialization.SerializationUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public final class Component implements Serializable {
    private final int address;
    private final int parentAddress;
    private final short componentID;
    private final Vector3f localPosition;
    private final Quaternionf localRotation;
    private final Input[] inputs;
    private final Output[] outputs;
    private final byte[] customData;

    public Component(int address, int parentAddress, short componentID, Vector3f localPosition, Quaternionf localRotation, Input[] inputs, Output[] outputs, byte[] customData) {
        this.address = address;
        this.parentAddress = parentAddress;
        this.componentID = componentID;
        this.localPosition = localPosition;
        this.localRotation = localRotation;
        this.inputs = inputs;
        this.outputs = outputs;
        this.customData = customData;
    }

    public int address() {
        return address;
    }

    public int parentAddress() {
        return parentAddress;
    }

    public short componentID() {
        return componentID;
    }

    public Vector3f localPosition() {
        return localPosition;
    }

    public Quaternionf localRotation() {
        return localRotation;
    }

    public Input[] inputs() {
        return inputs;
    }

    public Output[] outputs() {
        return outputs;
    }

    public byte[] customData() {
        return customData;
    }

    @Override
    public String toString() {
        return "Component[" +
                "address=" + address + ", " +
                "parentAddress=" + parentAddress + ", " +
                "componentID=" + componentID + ", " +
                "localPosition=" + localPosition + ", " +
                "localRotation=" + localRotation + ", " +
                "inputs=" + Arrays.toString(inputs) + ", " +
                "outputs=" + Arrays.toString(outputs) + ", " +
                "customData=" + Arrays.toString(customData) + ']';
    }

    public static Component deserialize(DataInput input, Component[] components) throws IOException {
        int address = input.readInt();
        int parentAddress = input.readInt();
        if (parentAddress != 0 && components[parentAddress] == null)
            throw new IllegalArgumentException("Parent component with ID " + parentAddress + " not found!");
        short componentID = input.readShort();
        Vector3f position = SerializationUtil.deserializeVector3f(input);
        Quaternionf rotation = SerializationUtil.deserializeQuaternionf(input);
        Input[] inputs = new Input[input.readUnsignedByte()];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = Input.deserialize(input);
        }
        Output[] outputs = new Output[input.readUnsignedByte()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = Output.deserialize(input);
        }
        byte[] customData = new byte[input.readInt()];
        input.readFully(customData);
        return new Component(address, parentAddress, componentID, position, rotation, inputs, outputs, customData);
    }

    public static Component fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"componentAddress", "parentAddress", "componentID", "localPosition", "localRotation", "inputs", "outputs", "customData"}, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.OBJECT, JsonNodeType.OBJECT, JsonNodeType.ARRAY, JsonNodeType.ARRAY, JsonNodeType.ARRAY});
        BigInteger componentAddress = JsonUtil.asUnsignedInteger(node.get("componentAddress"), BigInteger.valueOf(0xffffffffL));
        BigInteger parentAddress = JsonUtil.asUnsignedInteger(node.get("parentAddress"), BigInteger.valueOf(0xffffffffL));
        BigInteger componentID = JsonUtil.asUnsignedInteger(node.get("componentID"), BigInteger.valueOf(0xffff));
        return new Component((int) componentAddress.longValueExact(), (int) parentAddress.longValueExact(), (short) componentID.intValueExact(),
                JsonUtil.parseVector(node.get("localPosition")), JsonUtil.parseQuaternion(node.get("localRotation")),
                JsonUtil.parseArray(node.get("inputs"), 0, 0, Input[]::new, Input::fromJson),
                JsonUtil.parseArray(node.get("outputs"), 0, 0, Output[]::new, Output::fromJson),
                JsonUtil.parseByteArray(node.get("customData")));
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(address);
        output.writeInt(parentAddress);
        output.writeShort(componentID);
        SerializationUtil.serializeVector3f(output, localPosition);
        SerializationUtil.serializeQuaternionf(output, localRotation);
        output.writeByte(inputs.length);
        for (Input Input : inputs) {
            Input.serialize(output);
        }
        output.writeByte(outputs.length);
        for (Output Output : outputs) {
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

    @Override
    public JsonNode toJson() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("componentAddress", Integer.toUnsignedLong(address));
        result.put("parentAddress", Integer.toUnsignedLong(parentAddress));
        result.put("componentID", componentID);
        result.set("localPosition", JsonUtil.jsonifyVector3f(localPosition));
        result.set("localRotation", JsonUtil.jsonifyQuaternionf(localRotation));
        result.set("inputs", JsonUtil.jsonifyArray(inputs, Input::toJson));
        result.set("outputs", JsonUtil.jsonifyArray(outputs, Output::toJson));
        result.set("customData", JsonUtil.jsonifyByteArray(customData));
        return result;
    }
}
