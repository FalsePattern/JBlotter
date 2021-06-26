package com.github.falsepattern.jblotter.util.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonParser<T> {
    T fromJson(JsonNode node) throws JsonParseException;
}
