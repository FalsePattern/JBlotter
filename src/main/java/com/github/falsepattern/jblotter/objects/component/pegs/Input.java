package com.github.falsepattern.jblotter.objects.component.pegs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record Input(boolean exclusive, int circuitStateID) {
    public static Input deserialize(DataInput input) throws IOException {
        return new Input(input.readBoolean(), input.readInt());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(exclusive);
        output.writeInt(circuitStateID);
    }
}
