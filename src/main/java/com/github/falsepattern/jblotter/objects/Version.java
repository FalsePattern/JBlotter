package com.github.falsepattern.jblotter.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.StaticArrayRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record Version(int majorVersion, int minorVersion, int patchVersion, int buildVersion) implements Serializable {
    public static final NodeRule RULE = new StaticArrayRule(4, IntegerRule.POSITIVE_SIGNED_INT, IntegerRule.POSITIVE_SIGNED_INT, IntegerRule.POSITIVE_SIGNED_INT, IntegerRule.POSITIVE_SIGNED_INT);

    public static Version deserialize(DataInput input) throws IOException {
        return new Version(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    public static Version fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) RULE.verify(node);
        return new Version(node.get(0).intValue(), node.get(1).intValue(), node.get(2).intValue(), node.get(3).intValue());
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

    @Override
    public ArrayNode toEditableJson() {
        return toJson();
    }

    public int[] asArray() {
        return new int[]{majorVersion, minorVersion, patchVersion, buildVersion};
    }
}
