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
import java.util.Objects;

public final class GameVersion implements Serializable {
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;
    private final int buildVersion;

    public GameVersion(int majorVersion, int minorVersion, int patchVersion, int buildVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.buildVersion = buildVersion;
    }

    public int majorVersion() {
        return majorVersion;
    }

    public int minorVersion() {
        return minorVersion;
    }

    public int patchVersion() {
        return patchVersion;
    }

    public int buildVersion() {
        return buildVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        GameVersion that = (GameVersion) obj;
        return this.majorVersion == that.majorVersion &&
                this.minorVersion == that.minorVersion &&
                this.patchVersion == that.patchVersion &&
                this.buildVersion == that.buildVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorVersion, minorVersion, patchVersion, buildVersion);
    }

    @Override
    public String toString() {
        return "GameVersion[" +
                "majorVersion=" + majorVersion + ", " +
                "minorVersion=" + minorVersion + ", " +
                "patchVersion=" + patchVersion + ", " +
                "buildVersion=" + buildVersion + ']';
    }

    public static GameVersion deserialize(DataInput input) throws IOException {
        return new GameVersion(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    public static GameVersion fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyFixedLengthJsonArray(node, new JsonNodeType[]{JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        BigInteger major = JsonUtil.asUnsignedInteger(node.get(0), BigInteger.valueOf(Integer.MAX_VALUE));
        BigInteger minor = JsonUtil.asUnsignedInteger(node.get(1), BigInteger.valueOf(Integer.MAX_VALUE));
        BigInteger patch = JsonUtil.asUnsignedInteger(node.get(2), BigInteger.valueOf(Integer.MAX_VALUE));
        BigInteger build = JsonUtil.asUnsignedInteger(node.get(3), BigInteger.valueOf(Integer.MAX_VALUE));
        return new GameVersion(major.intValueExact(), minor.intValueExact(), patch.intValueExact(), build.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(majorVersion);
        output.writeInt(minorVersion);
        output.writeInt(patchVersion);
        output.writeInt(buildVersion);
    }

    public ArrayNode toJson() {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);
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
