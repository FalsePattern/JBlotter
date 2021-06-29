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

public record PegAddress(boolean input, int componentAddress, byte pegIndex) implements Serializable {
    public static PegAddress deserialize(DataInput input) throws IOException {
        return new PegAddress(input.readBoolean(), input.readInt(), input.readByte());
    }

    public static PegAddress fromJson(JsonNode node) throws JsonParseException {
        JsonUtil.verifyJsonObject(node, new String[]{"input", "componentAddress", "pegIndex"}, new JsonNodeType[]{JsonNodeType.BOOLEAN, JsonNodeType.NUMBER, JsonNodeType.NUMBER});
        var componentAddress = JsonUtil.asUnsignedInteger(node.get("componentAddress"), BigInteger.valueOf(0xffffffffL));
        var pegIndex = JsonUtil.asUnsignedInteger(node.get("pegIndex"), BigInteger.valueOf(0xff));
        return new PegAddress(node.get("input").booleanValue(), (int)componentAddress.longValueExact(), (byte)pegIndex.shortValueExact());
    }

    public void serialize(DataOutput output) throws IOException {
        output.writeBoolean(input);
        output.writeInt(componentAddress);
        output.writeByte(pegIndex);
    }

    @Override
    public ObjectNode toJson() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("input", input);
        result.put("componentAddress", Integer.toUnsignedLong(componentAddress));
        result.put("pegIndex", Byte.toUnsignedInt(pegIndex));
        return result;
    }

    @Override
    public ObjectNode toEditableJson() {
        return toJson();
    }
}
