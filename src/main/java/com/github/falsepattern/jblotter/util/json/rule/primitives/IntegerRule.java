package com.github.falsepattern.jblotter.util.json.rule.primitives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;

import java.math.BigInteger;

public class IntegerRule implements NodeRule {
    public static final IntegerRule POSITIVE_SIGNED_INT = new IntegerRule(BigInteger.ZERO, BigInteger.valueOf(Integer.MAX_VALUE));

    public static final IntegerRule UNSIGNED_INT = new IntegerRule(BigInteger.ZERO, BigInteger.valueOf(0xffffffffL));
    public static final IntegerRule UNSIGNED_SHORT = new IntegerRule(BigInteger.ZERO, BigInteger.valueOf(0xffff));
    public static final IntegerRule UNSIGNED_BYTE = new IntegerRule(BigInteger.ZERO, BigInteger.valueOf(0xff));
    private final BigInteger min;
    private final BigInteger max;
    public IntegerRule(BigInteger minimum, BigInteger maximum) {
        this.min = minimum;
        this.max = maximum;
    }

    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isNumber()) throw new JsonParseException("Expected NUMBER, got " + node.getNodeType().name() + "!");
        if (!node.isIntegralNumber()) throw new JsonParseException("Expected integer, got decimal!");
        var val = node.bigIntegerValue();
        if (val.compareTo(min) < 0 || val.compareTo(max) > 0) throw new JsonParseException("Expected integer between <" + min + ", " + max + ">, got " + val + "!");
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "NUMBER");
        result.put("boundedInteger", true);
        var bounds = new ObjectNode(JsonNodeFactory.instance);
        bounds.put("min", min);
        bounds.put("max", max);
        result.set("bounds", bounds);
        return result;
    }
}
