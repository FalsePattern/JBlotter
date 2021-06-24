package com.github.falsepattern.jblotter.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record GameVersion(int majorVersion, int minorVersion, int patchVersion, int buildVersion) {

    public static GameVersion deserialize(DataInput input) throws IOException {
        return new GameVersion(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(majorVersion);
        output.writeInt(minorVersion);
        output.writeInt(patchVersion);
        output.writeInt(buildVersion);
    }
}
