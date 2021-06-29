package com.github.falsepattern.jblotter.util.json.rule.primitives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;

public class BooleanRule implements NodeRule {
    public static final BooleanRule INSTANCE = new BooleanRule();
    private BooleanRule(){}
    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isBoolean()) throw new JsonParseException("Expected BOOLEAN, got " + node.getNodeType().name() + "!");
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "BOOLEAN");
        return result;
    }
}
