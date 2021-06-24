package com.github.falsepattern.jblotter.objects.component;

import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record Wire(PegAddress firstPoint, PegAddress secondPoint, int circuitStateID, float rotation) {
    public static Wire deserialize(DataInput input) throws IOException {
        return new Wire(PegAddress.deserialize(input), PegAddress.deserialize(input), input.readInt(), input.readFloat());
    }

    public void serialize(DataOutput output) throws IOException {
        firstPoint.serialize(output);
        secondPoint.serialize(output);
        output.writeInt(circuitStateID);
        output.writeFloat(rotation);
    }
}
