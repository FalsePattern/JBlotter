package com.github.falsepattern.jblotter.util.json.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;

public class StaticArrayRule implements NodeRule {
    private final int length;
    private final NodeRule[] subRules;
    public StaticArrayRule(int length, NodeRule... subRules) {
        this.length = length;
        if (subRules.length != length) throw new IllegalArgumentException("Tried to create static-sized array rule with mismatching length and rule count!");
        this.subRules = subRules;
    }

    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isArray()) throw new JsonParseException("Expected ARRAY, got " + node.getNodeType().name() + "!");
        int length = node.size();
        if (length != this.length) throw new JsonParseException("Expected array with " + this.length + " elements, got one with " + length + " elements!");
        for (int i = 0; i < length; i++) {
            try {
                subRules[i].verify(node.get(i));
            } catch (JsonParseException e) {
                throw new JsonParseException("Element #" + i + " of static array!", e);
            }
        }
    }

    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "STATIC_ARRAY");
        result.put("length", length);
        var subRules = new ArrayNode(JsonNodeFactory.instance);
        for (int i = 0; i < this.subRules.length; i++) {
            var subRule = new ObjectNode(JsonNodeFactory.instance);
            subRule.put("index", i);
            subRule.set("rule", this.subRules[i].asJsonRepresentation());
            subRules.add(subRule);
        }
        result.set("subRules", subRules);
        return result;
    }
}
