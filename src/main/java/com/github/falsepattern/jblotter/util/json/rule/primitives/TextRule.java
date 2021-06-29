package com.github.falsepattern.jblotter.util.json.rule.primitives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.NodeRule;

public class TextRule implements NodeRule {
    public static final TextRule INSTANCE = new TextRule();
    private TextRule(){}
    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isTextual()) throw new JsonParseException("Expected STRING, got " + node.getNodeType().name() + "!");
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "STRING");
        return result;
    }
}
