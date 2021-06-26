package com.github.falsepattern.jblotter.util.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface Jsonifier <T> {
    JsonNode toJson(T value);
}
