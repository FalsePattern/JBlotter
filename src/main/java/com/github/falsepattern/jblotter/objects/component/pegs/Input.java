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

public record Input(boolean exclusive, int circuitStateID) implements Serializable {
    public static final NodeRule RULE = new ObjectRule(new String[]{"exclusive", "circuitStateID"}, new NodeRule[]{BooleanRule.INSTANCE, IntegerRule.POSITIVE_SIGNED_INT}, true);
    public static final NodeRule EDITABLE_RULE = new ObjectRule(new String[]{"exclusive"}, new NodeRule[]{BooleanRule.INSTANCE}, true);
    public static Input deserialize(DataInput input) throws IOException {
        return new Input(input.readBoolean(), input.readInt());
    }

    public static Input fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) RULE.verify(node);
        return new Input(node.get("exclusive").booleanValue(), node.get("circuitStateID").intValue());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(exclusive);
        output.writeInt(circuitStateID);
    }

    @Override
    public ObjectNode toJson() {
        var result = toEditableJson();
        result.put("circuitStateID", circuitStateID);
        return result;
    }

    @Override
    public ObjectNode toEditableJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("exclusive", exclusive);
        return result;
    }
}
