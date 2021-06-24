package com.github.falsepattern.jblotter.objects.component.pegs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record Output(int circuitStateID) {
    public static Output deserialize(DataInput input) throws IOException {
        return new Output(input.readInt());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(circuitStateID);
    }
}
