package com.github.falsepattern.jblotter.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
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

    public static <T> T[] parseArray(JsonNode node, int inputOffset, int outputOffset, Function<Integer, T[]> arrayConstructor, JsonParser<T> parser) throws JsonParseException {
        if (!node.isArray()) throw new JsonParseException("Not a json array:\n" + node.toPrettyString());
        if (node.size() < inputOffset) throw new JsonParseException("Json array size smaller than offset! Array size: " + node.size() + ", offset: " + inputOffset + ". Array:\n" + node.toPrettyString());
        if (node.size() == inputOffset) return arrayConstructor.apply(0);
        int readLength = node.size() - inputOffset;
        var result = arrayConstructor.apply(outputOffset + readLength);
        for (int i = 0; i < readLength; i++) {
            result[i + outputOffset] = parser.fromJson(node.get(i + inputOffset));
        }
        return result;
    }

    public static byte[] parseByteArray(JsonNode node) throws JsonParseException{
        if (!node.isArray()) throw new JsonParseException("Not a json array:\n" + node.toPrettyString());
        var result = new byte[node.size()];
        for (int i = 0; i < result.length; i++) {
            var n = asUnsignedInteger(node.get(i), BigInteger.valueOf(0xff));
            result[i] = (byte)n.shortValueExact();
        }
        return result;
    }

    public static Vector3f parseVector(JsonNode node) throws JsonParseException {
        verifyJsonObject(node, new String[]{"x", "y", "z"}, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        return new Vector3f(
                node.get("x").floatValue(),
                node.get("y").floatValue(),
                node.get("z").floatValue()
        );
    }

    public static Quaternionf parseQuaternion(JsonNode node) throws JsonParseException {
        verifyJsonObject(node, new String[]{"x", "y", "z", "w"}, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        return new Quaternionf(
                node.get("x").floatValue(),
                node.get("y").floatValue(),
                node.get("z").floatValue(),
                node.get("w").floatValue()
        );
    }

    public static void verifyJsonObject(JsonNode node, String[] desiredFields, JsonNodeType[] desiredFieldTypes) throws JsonParseException {
        if (!node.isObject()) throw new JsonParseException("Not a json object:\n" + node.toPrettyString());
        for (int i = 0; i < desiredFields.length; i++) {
            if (!node.has(desiredFields[i])) throw new JsonParseException("Json object is missing field \"" + desiredFields[i] + "\":\n" + node.toPrettyString());
            var f = node.get(desiredFields[i]);
            if (f.getNodeType() != desiredFieldTypes[i]) throw new JsonParseException("Json field type mismatch! Wanted: " + desiredFieldTypes[i].name() + ", got: " + f.getNodeType().name() + "! Field \"" + desiredFields[i] + "\" in:\n" + node.toPrettyString());
        }
    }

    public static void verifyFixedLengthJsonArray(JsonNode node, JsonNodeType[] desiredFieldTypes) throws JsonParseException {
        if (!node.isArray()) throw new JsonParseException("Not a json array:\n" + node.toPrettyString());
        if (node.size() != desiredFieldTypes.length) throw new JsonParseException("Fixed-size json array longer than wanted size! Wanted: " + desiredFieldTypes.length + ", got: " + node.size() + "!\n" + node.toPrettyString());
        for (int i = 0; i < desiredFieldTypes.length; i++) {
            var obj = node.get(i);
            if (obj.getNodeType() != desiredFieldTypes[i]) throw new JsonParseException("Json field type mismatch! Wanted: " + desiredFieldTypes[i].name() + ", got: " + obj.getNodeType().name() + "! Field " + i + " in:\n" + node.toPrettyString());
        }
    }

    public static void verifyDynamicLengthJsonArray(JsonNode node, JsonNodeType desiredFieldType) throws JsonParseException {
        if (!node.isArray()) throw new JsonParseException("Not a json array:\n" + node.toPrettyString());
        int size = node.size();
        for (int i = 0; i < size; i++) {
            var obj = node.get(i);
            if (obj.getNodeType() != desiredFieldType) throw new JsonParseException("Json field type mismatch! Wanted: " + desiredFieldType.name() + ", got: " + obj.getNodeType().name() + "! Field " + i + " in:\n" + node.toPrettyString());
        }
    }

    public static BigInteger asUnsignedInteger(JsonNode node, BigInteger maxValue) throws JsonParseException {
        if (!node.isNumber()) throw new JsonParseException("Tried to parse json node as unsigned integer:\n" + node.toPrettyString());
        if (!node.isIntegralNumber()) throw new JsonParseException("Tried to parse decimal number as unsigned integer:\n" + node.toPrettyString());
        var val = node.bigIntegerValue();
        if (val.signum() == -1) throw new JsonParseException("Tried to parse negative number as unsigned value: " + val);
        if (val.compareTo(maxValue) > 0) throw new JsonParseException("Tried to parse unsigned number greater than max value! Maximum: " + maxValue + ", number: " + val);
        return val;
    }
}
