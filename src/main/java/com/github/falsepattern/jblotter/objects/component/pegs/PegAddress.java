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

public final class PegAddress implements Serializable {
    private final boolean input;
    private final int componentAddress;
    private final byte pegIndex;

    public PegAddress(boolean input, int componentAddress, byte pegIndex) {
        this.input = input;
        this.componentAddress = componentAddress;
        this.pegIndex = pegIndex;
    }

    public boolean input() {
        return input;
    }

    public int componentAddress() {
        return componentAddress;
    }

    public byte pegIndex() {
        return pegIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PegAddress that = (PegAddress) obj;
        return this.input == that.input &&
                this.componentAddress == that.componentAddress &&
                this.pegIndex == that.pegIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, componentAddress, pegIndex);
    }

    @Override
    public String toString() {
        return "PegAddress[" +
                "input=" + input + ", " +
                "componentAddress=" + componentAddress + ", " +
                "pegIndex=" + pegIndex + ']';
    }

    public static PegAddress deserialize(DataInput input) throws IOException {
        return new PegAddress(input.readBoolean(), input.readInt(), input.readByte());
    }

    public static PegAddress fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"input", "componentAddress", "pegIndex"}, new JsonNodeType[]{JsonNodeType.BOOLEAN, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        BigInteger componentAddress = JsonUtil.asUnsignedInteger(node.get("componentAddress"), BigInteger.valueOf(0xffffffffL));
        BigInteger pegIndex = JsonUtil.asUnsignedInteger(node.get("pegIndex"), BigInteger.valueOf(0xff));
        return new PegAddress(node.get("input").booleanValue(), (int) componentAddress.longValueExact(), (byte) pegIndex.shortValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(input);
        output.writeInt(componentAddress);
        output.writeByte(pegIndex);
    }

    @Override
    public JsonNode toJson() {
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        result.put("input", input);
        result.put("componentAddress", Integer.toUnsignedLong(componentAddress));
        result.put("pegIndex", Byte.toUnsignedInt(pegIndex));
        return result;
    }
}
