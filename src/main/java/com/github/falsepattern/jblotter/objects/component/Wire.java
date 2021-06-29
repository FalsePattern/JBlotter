package com.github.falsepattern.jblotter.objects.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.DecimalRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

public record Wire(PegAddress firstPoint, PegAddress secondPoint, int circuitStateID, float rotation) implements Serializable {
    public static final NodeRule RULE = new ObjectRule(new String[]{"firstPoint", "secondPoint", "circuitStateID", "rotation"}, new NodeRule[]{PegAddress.RULE, PegAddress.RULE, IntegerRule.POSITIVE_SIGNED_INT, DecimalRule.INSTANCE}, true);
    public static final NodeRule EDITABLE_RULE = new ObjectRule(new String[]{"firstPoint", "secondPoint", "rotation"}, new NodeRule[]{PegAddress.RULE, PegAddress.RULE, DecimalRule.INSTANCE}, true);
    public static Wire deserialize(DataInput input) throws IOException {
        return new Wire(PegAddress.deserialize(input), PegAddress.deserialize(input), input.readInt(), input.readFloat());
    }

    public static Wire fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) RULE.verify(node);
        return new Wire(PegAddress.fromJson(node.get("firstPoint"), true), PegAddress.fromJson(node.get("secondPoint"), true), node.get("circuitStateID").intValue(), node.get("rotation").floatValue());
    }

    public void serialize(DataOutput output) throws IOException {
        firstPoint.serialize(output);
        secondPoint.serialize(output);
        output.writeInt(circuitStateID);
        output.writeFloat(rotation);
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
        result.set("firstPoint", firstPoint.toJson());
        result.set("secondPoint", secondPoint.toJson());
        result.put("rotation", rotation);
        return result;
    }
}
