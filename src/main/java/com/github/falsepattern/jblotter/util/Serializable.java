package com.github.falsepattern.jblotter.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutput;
import java.io.IOException;

public interface Serializable {
    void serialize(DataOutput output) throws IOException;
    JsonNode toJson();
}
