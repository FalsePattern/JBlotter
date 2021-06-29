package com.github.falsepattern.jblotter.util.json.rule.primitives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;

import java.math.BigDecimal;

public class DecimalRule implements NodeRule {
    public static final DecimalRule INSTANCE = new DecimalRule();
    private DecimalRule(){}
    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isNumber()) throw new JsonParseException("Expected NUMBER, got " + node.getNodeType().name() + "!");
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "NUMBER");
        result.put("boundedInteger", false);
        return result;
    }
}
