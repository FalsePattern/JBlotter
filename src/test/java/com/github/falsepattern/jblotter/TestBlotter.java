package com.github.falsepattern.jblotter;

import com.github.falsepattern.jblotter.objects.BlotterFile;
import com.github.falsepattern.jblotter.util.EndianInputStream;
import com.github.falsepattern.jblotter.util.EndianOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TestBlotter {

    private void test(boolean world) {
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 256; i++) {
                var file = RandomSaveGenerator.generateSave(world);
                var out = new ByteArrayOutputStream();
                var data = new EndianOutputStream(out, true);
                file.serialize(data);
                data.flush();
                var bytes = out.toByteArray();
                var in = new ByteArrayInputStream(bytes);
                var dataIn = new EndianInputStream(in, true);
                var deserializedFile = BlotterFile.deserialize(dataIn);
                Assertions.assertEquals(file, deserializedFile);
                Assertions.assertEquals(file.hashCode(), deserializedFile.hashCode());
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
