package com.github.falsepattern.jblotter.util.json;

public class JsonParseException extends Exception{
    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Exception cause) {
        super(message, cause);
    }
}
