package com.github.falsepattern.jblotter;

import com.github.falsepattern.jblotter.objects.BlotterFile;
import com.github.falsepattern.jblotter.util.serialization.EndianInputStream;
import com.github.falsepattern.jblotter.util.serialization.EndianOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TestBlotter {

    private void test(boolean world) {
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 256; i++) {
                BlotterFile file = RandomSaveGenerator.generateSave(world);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                EndianOutputStream data = new EndianOutputStream(out, true);
                file.serialize(data);
                data.flush();
                byte[] bytes = out.toByteArray();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                EndianInputStream dataIn = new EndianInputStream(in, true);
                BlotterFile deserializedFile = BlotterFile.deserialize(dataIn);
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
