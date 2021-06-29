package com.github.falsepattern.jblotter.util.json.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.primitives.IntegerRule;

public class DynamicArrayRule implements NodeRule {
    public static final DynamicArrayRule BYTE_ARRAY = new DynamicArrayRule(IntegerRule.UNSIGNED_BYTE);
    private final NodeRule subRule;
    public DynamicArrayRule(NodeRule subRule) {
        this.subRule = subRule;
    }
    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isArray()) throw new JsonParseException("Expected ARRAY, got " + node.getNodeType().name() + "!");
        int length = node.size();
        for (int i = 0; i < length; i++) {
            try {
                subRule.verify(node.get(i));
            } catch (JsonParseException e) {
                throw new JsonParseException("Element #" + i + " of dynamic array:", e);
            }
        }
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "DYNAMIC_ARRAY");
        result.set("subRule", subRule.asJsonRepresentation());
        return result;
    }
}
