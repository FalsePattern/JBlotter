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
import java.util.Objects;

public final class Wire implements Serializable {
    private final PegAddress firstPoint;
    private final PegAddress secondPoint;
    private final int circuitStateID;
    private final float rotation;

    public Wire(PegAddress firstPoint, PegAddress secondPoint, int circuitStateID, float rotation) {
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
        this.circuitStateID = circuitStateID;
        this.rotation = rotation;
    }

    public PegAddress firstPoint() {
        return firstPoint;
    }

    public PegAddress secondPoint() {
        return secondPoint;
    }

    public int circuitStateID() {
        return circuitStateID;
    }

    public float rotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Wire that = (Wire) obj;
        return Objects.equals(this.firstPoint, that.firstPoint) &&
                Objects.equals(this.secondPoint, that.secondPoint) &&
                this.circuitStateID == that.circuitStateID &&
                Float.floatToIntBits(this.rotation) == Float.floatToIntBits(that.rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstPoint, secondPoint, circuitStateID, rotation);
    }

    @Override
    public String toString() {
        return "Wire[" +
                "firstPoint=" + firstPoint + ", " +
                "secondPoint=" + secondPoint + ", " +
                "circuitStateID=" + circuitStateID + ", " +
                "rotation=" + rotation + ']';
    }

    public static Wire deserialize(DataInput input) throws IOException {
        return new Wire(PegAddress.deserialize(input), PegAddress.deserialize(input), input.readInt(), input.readFloat());
    }

    public static Wire fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"firstPoint", "secondPoint", "circuitStateID", "rotation"}, new JsonNodeType[]{JsonNodeType.OBJECT, JsonNodeType.OBJECT, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        BigInteger circuitStateID = JsonUtil.asUnsignedInteger(node.get("circuitStateID"), BigInteger.valueOf(Integer.MAX_VALUE));
        return new Wire(PegAddress.fromJson(node.get("firstPoint")), PegAddress.fromJson(node.get("secondPoint")), circuitStateID.intValueExact(), node.get("rotation").floatValue());
    }

    public void serialize(DataOutput output) throws IOException {
        firstPoint.serialize(output);
        secondPoint.serialize(output);
        output.writeInt(circuitStateID);
        output.writeFloat(rotation);
    }

    @Override
    public JsonNode toJson() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.set("firstPoint", firstPoint.toJson());
        result.set("secondPoint", secondPoint.toJson());
        result.put("circuitStateID", circuitStateID);
        result.put("rotation", rotation);
        return result;
    }
}
