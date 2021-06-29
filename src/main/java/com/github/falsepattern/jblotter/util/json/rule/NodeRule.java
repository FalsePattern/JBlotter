package com.github.falsepattern.jblotter.util.json.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.falsepattern.jblotter.util.json.JsonParseException;

public interface NodeRule {
    void verify(JsonNode node) throws JsonParseException;

    JsonNode asJsonRepresentation();
}
