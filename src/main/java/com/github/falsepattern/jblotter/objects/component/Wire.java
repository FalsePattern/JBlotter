package com.github.falsepattern.jblotter.objects.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.Serializable;
import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.JsonUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

public record Wire(PegAddress firstPoint, PegAddress secondPoint, int circuitStateID, float rotation) implements Serializable {
    public static Wire deserialize(DataInput input) throws IOException {
        return new Wire(PegAddress.deserialize(input), PegAddress.deserialize(input), input.readInt(), input.readFloat());
    }

    public static Wire fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"firstPoint", "secondPoint", "circuitStateID", "rotation"}, new JsonNodeType[]{JsonNodeType.OBJECT, JsonNodeType.OBJECT, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        var circuitStateID = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Wire(PegAddress.fromJson(node.get("firstPoint")), PegAddress.fromJson(node.get("secondPoint")), circuitStateID.intValueExact(), node.get("rotation").floatValue());
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
