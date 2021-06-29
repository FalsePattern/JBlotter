package com.github.falsepattern.jblotter.objects.component.pegs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.BooleanRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

public record PegAddress(boolean input, int componentAddress, byte pegIndex) implements Serializable, Comparable<PegAddress> {
    public static final NodeRule RULE = new ObjectRule(new String[]{"input", "componentAddress", "pegIndex"}, new NodeRule[]{BooleanRule.INSTANCE, IntegerRule.UNSIGNED_INT, IntegerRule.UNSIGNED_INT}, true);
    public static PegAddress deserialize(DataInput input) throws IOException {
        return new PegAddress(input.readBoolean(), input.readInt(), input.readByte());
    }

    public static PegAddress fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified)RULE.verify(node);
        return new PegAddress(node.get("input").booleanValue(), (int)node.get("componentAddress").longValue(), (byte)node.get("pegIndex").intValue());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(input);
        output.writeInt(componentAddress);
        output.writeByte(pegIndex);
    }

    @Override
    public ObjectNode toJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("input", input);
        result.put("componentAddress", Integer.toUnsignedLong(componentAddress));
        result.put("pegIndex", Byte.toUnsignedInt(pegIndex));
        return result;
    }

    @Override
    public ObjectNode toEditableJson() {
        return toJson();
    }

    @Override
    public int compareTo(PegAddress o) {
        if (input == o.input) {
            if (componentAddress == o.componentAddress) {
                return pegIndex - o.pegIndex;
            } else {
                return componentAddress - o.componentAddress;
            }
        } else {
            return input ? 1 : -1;
        }
    }
}
