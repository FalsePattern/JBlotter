package com.github.falsepattern.jblotter.objects.component;

import com.github.falsepattern.jblotter.objects.component.pegs.Input;
import com.github.falsepattern.jblotter.objects.component.pegs.Output;
import com.github.falsepattern.jblotter.util.SerializationUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Component(int address, int parentAddress, short componentID, Vector3f localPosition, Quaternionf localRotation, Input[] inputs, Output[] outputs, byte[] customData) {
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
}
