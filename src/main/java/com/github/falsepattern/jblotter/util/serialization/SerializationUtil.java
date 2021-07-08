package com.github.falsepattern.jblotter.util.serialization;

import com.github.falsepattern.jblotter.util.Serializable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    public static void serializeArrayWithoutLength(DataOutput output, Serializable[] array) throws IOException {
        serializeArrayWithoutLength(output, array, 0, array.length);
    }

    public static void serializeArrayWithoutLength(DataOutput output, Serializable[] array, int start, int endNonInclusive) throws IOException {
        for (int i = start; i < endNonInclusive; i++) {
            array[i].serialize(output);
        }
    }

    public static String deserializeString(DataInput input) throws IOException {
        var utf8Bytes = input.readInt();
        var buffer = new byte[utf8Bytes];
        input.readFully(buffer, 0, utf8Bytes);
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buffer)).toString();
    }

    public static void serializeString(DataOutput output, String str) throws IOException {
        var utf8 = str.getBytes(StandardCharsets.UTF_8);
        var utf8Bytes = utf8.length;
        output.writeInt(utf8Bytes);
        output.write(utf8);
    }

}
