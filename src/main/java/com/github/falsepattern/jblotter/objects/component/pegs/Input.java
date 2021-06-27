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

public final class Input implements Serializable {
    private final boolean exclusive;
    private final int circuitStateID;

    public Input(boolean exclusive, int circuitStateID) {
        this.exclusive = exclusive;
        this.circuitStateID = circuitStateID;
    }

    public boolean exclusive() {
        return exclusive;
    }

    public int circuitStateID() {
        return circuitStateID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Input that = (Input) obj;
        return this.exclusive == that.exclusive &&
                this.circuitStateID == that.circuitStateID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exclusive, circuitStateID);
    }

    @Override
    public String toString() {
        return "Input[" +
                "exclusive=" + exclusive + ", " +
                "circuitStateID=" + circuitStateID + ']';
    }

    public static Input deserialize(DataInput input) throws IOException {
        return new Input(input.readBoolean(), input.readInt());
    }

    public static Input fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"exclusive", "circuitStateID"}, new JsonNodeType[]{JsonNodeType.BOOLEAN, JsonNodeType.NUMBER});
        BigInteger circuitStateID = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Input(node.get("exclusive").booleanValue(), circuitStateID.intValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(exclusive);
        output.writeInt(circuitStateID);
    }

    @Override
    public JsonNode toJson() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("circuitStateID", circuitStateID);
        result.put("exclusive", exclusive);
        return result;
    }
}
