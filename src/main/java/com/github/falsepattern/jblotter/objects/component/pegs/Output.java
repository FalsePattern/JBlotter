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
import java.util.Objects;

public final class Output implements Serializable {
    private final int circuitStateID;

    public Output(int circuitStateID) {
        this.circuitStateID = circuitStateID;
    }

    public int circuitStateID() {
        return circuitStateID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Output that = (Output) obj;
        return this.circuitStateID == that.circuitStateID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(circuitStateID);
    }

    @Override
    public String toString() {
        return "Output[" +
                "circuitStateID=" + circuitStateID + ']';
    }

    public static Output deserialize(DataInput input) throws IOException {
        return new Output(input.readInt());
    }

    public static Output fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"circuitStateID"}, new JsonNodeType[]{JsonNodeType.NUMBER});
        BigInteger num = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Output(num.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeInt(circuitStateID);
    }

    @Override
    public JsonNode toJson() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("circuitStateID", circuitStateID);
        return result;
    }
}
