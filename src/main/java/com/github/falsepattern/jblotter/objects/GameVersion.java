package com.github.falsepattern.jblotter.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

public record GameVersion(int majorVersion, int minorVersion, int patchVersion, int buildVersion) implements Serializable {

    public static GameVersion deserialize(DataInput input) throws IOException {
        return new GameVersion(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    public static GameVersion fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyFixedLengthJsonArray(node, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        var major = JsonUtil.asUnsignedInteger(node.get(0), BigInteger.valueOf(Integer.MAX_VALUE));
        var minor = JsonUtil.asUnsignedInteger(node.get(1), BigInteger.valueOf(Integer.MAX_VALUE));
        var patch = JsonUtil.asUnsignedInteger(node.get(2), BigInteger.valueOf(Integer.MAX_VALUE));
        var build = JsonUtil.asUnsignedInteger(node.get(3), BigInteger.valueOf(Integer.MAX_VALUE));
        return new GameVersion(major.intValueExact(), minor.intValueExact(), patch.intValueExact(), build.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(majorVersion);
        output.writeInt(minorVersion);
        output.writeInt(patchVersion);
        output.writeInt(buildVersion);
    }

    public ArrayNode toJson() {
        var result = new ArrayNode(JsonNodeFactory.instance);
        result.add(Integer.toUnsignedLong(majorVersion));
        result.add(Integer.toUnsignedLong(minorVersion));
        result.add(Integer.toUnsignedLong(patchVersion));
        result.add(Integer.toUnsignedLong(buildVersion));
        return result;
    }

    public int[] asArray() {
        return new int[]{majorVersion, minorVersion, patchVersion, buildVersion};
    }
}
