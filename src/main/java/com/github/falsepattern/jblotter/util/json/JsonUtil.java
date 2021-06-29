package com.github.falsepattern.jblotter.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.rule.DynamicArrayRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.math.BigInteger;
import java.util.function.Function;

public class JsonUtil {


    public static <T> ArrayNode jsonifyArray(T[] array, Jsonifier<T> converter) {
        return jsonifyArray(array, 0, array.length, converter);
    }

    public static <T> ArrayNode jsonifyArray(T[] array, int start, int endNonInclusive, Jsonifier<T> converter) {
        var result = new ArrayNode(JsonNodeFactory.instance);
        for (int i = start; i < endNonInclusive; i++) {
            result.add(converter.toJson(array[i]));
        }
        return result;
    }

    public static ArrayNode jsonifyByteArray(byte[] array) {
        return jsonifyByteArray(array, 0, array.length);
    }

    public static ArrayNode jsonifyByteArray(byte[] array, int start, int endNonInclusive) {
        var result = new ArrayNode(JsonNodeFactory.instance);
        for (int i = start; i < endNonInclusive; i++) {
            result.add(Byte.toUnsignedInt(array[i]));
        }
        return result;
    }

    public static ObjectNode jsonifyVector3f(Vector3f vector) {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("x", vector.x);
        result.put("y", vector.y);
        result.put("z", vector.z);
        return result;
    }

    public static ObjectNode jsonifyQuaternionf(Quaternionf quaternion) {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("x", quaternion.x);
        result.put("y", quaternion.y);
        result.put("z", quaternion.z);
        result.put("w", quaternion.w);
        return result;
    }

    public static <T> T[] parseArrayNoVerify(JsonNode node, int inputOffset, int outputOffset, Function<Integer, T[]> arrayConstructor, JsonParser<T> parser) throws JsonParseException {
        if (node.size() < inputOffset) throw new JsonParseException("Json array size smaller than offset! Array size: " + node.size() + ", offset: " + inputOffset + ". Array:\n" + node.toPrettyString());
        if (node.size() == inputOffset) return arrayConstructor.apply(0);
        int readLength = node.size() - inputOffset;
        var result = arrayConstructor.apply(outputOffset + readLength);
        for (int i = 0; i < readLength; i++) {
            result[i + outputOffset] = parser.fromJson(node.get(i + inputOffset));
        }
        return result;
    }

    public static byte[] parseByteArray(JsonNode node, boolean verified) throws JsonParseException{
        if (!verified) DynamicArrayRule.BYTE_ARRAY.verify(node);
        var result = new byte[node.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)node.get(i).intValue();
        }
        return result;
    }

    public static Vector3f parseVector(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) ObjectRule.RULE_VEC3.verify(node);
        return new Vector3f(
                node.get("x").floatValue(),
                node.get("y").floatValue(),
                node.get("z").floatValue()
        );
    }

    public static Quaternionf parseQuaternion(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) ObjectRule.RULE_VEC3.verify(node);
        return new Quaternionf(
                node.get("x").floatValue(),
                node.get("y").floatValue(),
                node.get("z").floatValue(),
                node.get("w").floatValue()
        );
    }
}
