package com.github.falsepattern.jblotter;

import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.objects.BlotterFile;
import com.github.falsepattern.jblotter.util.serialization.EndianInputStream;
import com.github.falsepattern.jblotter.util.serialization.EndianOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TestJson {
    private void test(boolean world) {
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 256; i++) {
                var file = RandomSaveGenerator.generateSave(world);
                var json = file.toJson().toString();
                var dFile = BlotterFile.fromJson(new ObjectMapper().readValue(json, ObjectNode.class));
                Assertions.assertEquals(file.saveFormatVersion(), dFile.saveFormatVersion());
                Assertions.assertEquals(file.gameVersion(), dFile.gameVersion());
                Assertions.assertEquals(file.isWorld(), dFile.isWorld());
                Assertions.assertArrayEquals(file.componentIDs(), dFile.componentIDs());
                Assertions.assertArrayEquals(file.components(), dFile.components());
                Assertions.assertArrayEquals(file.wires(), dFile.wires());
                Assertions.assertEquals(file.worldCircuitStates(), dFile.worldCircuitStates());
                Assertions.assertArrayEquals(file.subassemblyCircuitStates(), dFile.subassemblyCircuitStates());
                Assertions.assertEquals(file, dFile);
                Assertions.assertEquals(file.hashCode(), dFile.hashCode());
            }
        });
    }

    @Test
    public void testWorlds() {
        test(true);
    }

    @Test
    public void testSubassembly() {
        test(false);
    }
}
