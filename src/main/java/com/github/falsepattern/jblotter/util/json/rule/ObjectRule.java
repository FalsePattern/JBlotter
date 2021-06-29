package com.github.falsepattern.jblotter.util.json.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;
import com.github.falsepattern.jblotter.util.json.rule.primitives.DecimalRule;

import java.util.HashMap;
import java.util.Map;

public class ObjectRule implements NodeRule{
    public static final ObjectRule RULE_VEC3 = new ObjectRule(new String[]{"x", "y", "z"}, new NodeRule[]{DecimalRule.INSTANCE, DecimalRule.INSTANCE, DecimalRule.INSTANCE}, false);
    public static final ObjectRule RULE_QUATERNION = new ObjectRule(new String[]{"x", "y", "z", "w"}, new NodeRule[]{DecimalRule.INSTANCE, DecimalRule.INSTANCE, DecimalRule.INSTANCE, DecimalRule.INSTANCE}, false);
    private final Map<String, NodeRule> subRules = new HashMap<>();
    private final boolean strict;
    public ObjectRule(String[] fields, NodeRule[] fieldRules, boolean strict) {
        if (fields.length != fieldRules.length) throw new IllegalArgumentException("Object rule field name count and field rule count mismatch!");
        for (int i = 0; i < fields.length; i++) {
            subRules.put(fields[i], fieldRules[i]);
        }
        this.strict = strict;
    }
    @Override
    public void verify(JsonNode node) throws JsonParseException {
        if (!node.isObject()) throw new JsonParseException("Expected OBJECT, got " + node.getNodeType().name());
        if (strict) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                if (!subRules.containsKey(field.getKey())) throw new JsonParseException("Extraneous field " + field.getKey() + " in object with strict checking!");
            }
        }
        for (var rule: subRules.entrySet()) {
            var key = rule.getKey();
            if (!node.has(key)) {
                throw new JsonParseException("Missing field " + key + " from object!");
            } else {
                try {
                    rule.getValue().verify(node.get(key));
                } catch (JsonParseException e) {
                    throw new JsonParseException("Field " + key + " of object!", e);
                }
            }
        }
    }

    public static ObjectRule join(ObjectRule a, ObjectRule b, boolean strict) {
        var rules = new HashMap<String, NodeRule>();
        for (var entry: a.subRules.entrySet()) {
            rules.put(entry.getKey(), entry.getValue());
        }
        for (var entry: b.subRules.entrySet()) {
            if (rules.containsKey(entry.getKey())) throw new IllegalArgumentException("Rule conflict while trying to join object rules: " + entry.getKey());
            rules.put(entry.getKey(), entry.getValue());
        }
        var fields = new String[rules.size()];
        var fieldRules = new NodeRule[rules.size()];
        int i = 0;
        for (var entry: rules.entrySet()) {
            fields[i] = entry.getKey();
            fieldRules[i] = entry.getValue();
            i++;
        }
        return new ObjectRule(fields, fieldRules, strict);
    }

    @Override
    public JsonNode asJsonRepresentation() {
        var result = new ObjectNode(JsonNodeFactory.instance);
        result.put("nodeType", "OBJECT");
        result.put("strict", strict);
        var subRules = new ArrayNode(JsonNodeFactory.instance);
        for (var entry: this.subRules.entrySet()) {
            var subRule = new ObjectNode(JsonNodeFactory.instance);
            subRule.put("field", entry.getKey());
            subRule.set("rule", entry.getValue().asJsonRepresentation());
            subRules.add(subRule);
        }
        result.set("subRules", subRules);
        return result;
    }
}
