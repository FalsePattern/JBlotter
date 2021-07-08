package com.github.falsepattern.jblotter.objects.component.pegs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;
import com.github.falsepattern.jblotter.util.json.rule.ObjectRule;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record Input(int circuitStateID) implements Serializable {
    public static final NodeRule RULE = new ObjectRule(new String[]{"circuitStateID"}, new NodeRule[]{IntegerRule.POSITIVE_SIGNED_INT}, true);
    public static final NodeRule EDITABLE_RULE = new ObjectRule(new String[]{}, new NodeRule[]{}, true);
    public static Input deserialize(DataInput input) throws IOException {
        return new Input(input.readInt());
    }

    public static Input fromJson(JsonNode node, boolean verified) throws JsonParseException {
        if (!verified) RULE.verify(node);
        return new Input(node.get("circuitStateID").intValue());
    }

    public void serialize(DataOutput output) throws IOException {
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
        return new ObjectNode(JsonNodeFactory.instance);
    }
}
