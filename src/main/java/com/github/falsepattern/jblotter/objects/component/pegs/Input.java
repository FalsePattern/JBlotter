package com.github.falsepattern.jblotter.objects.component.pegs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

public record Input(boolean exclusive, int circuitStateID) implements Serializable {
    public static Input deserialize(DataInput input) throws IOException {
        return new Input(input.readBoolean(), input.readInt());
    }

    public static Input fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"exclusive", "circuitStateID"}, new JsonNodeType[]{JsonNodeType.BOOLEAN, JsonNodeType.NUMBER});
        var circuitStateID = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Input(node.get("exclusive").booleanValue(), circuitStateID.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(exclusive);
        output.writeInt(circuitStateID);
    }

    @Override
    public JsonNode toJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("circuitStateID", circuitStateID);
        result.put("exclusive", exclusive);
        return result;
    }
}
