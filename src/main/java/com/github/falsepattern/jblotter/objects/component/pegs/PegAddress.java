package com.github.falsepattern.jblotter.objects.component.pegs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record PegAddress(boolean input, int componentAddress, byte pegIndex) {
    public static PegAddress deserialize(DataInput input) throws IOException {
        return new PegAddress(input.readBoolean(), input.readInt(), input.readByte());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(input);
        output.writeInt(componentAddress);
        output.writeByte(pegIndex);
    }
}
