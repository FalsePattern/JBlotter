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
import java.util.BitSet;

public record Output(int circuitStateID) implements Serializable {
    public static Output deserialize(DataInput input) throws IOException {
        return new Output(input.readInt());
    }

    public static Output fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"circuitStateID"}, new JsonNodeType[]{JsonNodeType.NUMBER});
        var num = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Output(num.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(circuitStateID);
    }

    @Override
    public ObjectNode toJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("circuitStateID", circuitStateID);
        return result;
    }

    public ObjectNode toEditableJson(BitSet circuitStates) {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("powered", circuitStates.get(circuitStateID));
        return result;
    }

    public JsonNode toEditableJson() {
        throw new UnsupportedOperationException("Use toEditableJson(BitSet) for serializing outputs! They require context about circuit states!");
    }
}
