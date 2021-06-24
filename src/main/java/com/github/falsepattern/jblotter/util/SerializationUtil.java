package com.github.falsepattern.jblotter.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SerializationUtil {
    public static Vector3f deserializeVector3f(DataInput input) throws IOException {
        return new Vector3f(input.readFloat(), input.readFloat(), input.readFloat());
    }

    public static void serializeVector3f(DataOutput output, Vector3f position) throws IOException {
        output.writeFloat(position.x);
        output.writeFloat(position.y);
        output.writeFloat(position.z);
    }

    public static Quaternionf deserializeQuaternionf(DataInput input) throws IOException {
        return new Quaternionf(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
    }

    public static void serializeQuaternionf(DataOutput output, Quaternionf rotation) throws IOException {
        output.writeFloat(rotation.x);
        output.writeFloat(rotation.y);
        output.writeFloat(rotation.z);
        output.writeFloat(rotation.w);
    }
}
