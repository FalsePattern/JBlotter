package com.github.falsepattern.jblotter;

import com.github.falsepattern.jblotter.objects.BlotterFile;
import com.github.falsepattern.jblotter.objects.Version;
import com.github.falsepattern.jblotter.objects.component.Component;
import com.github.falsepattern.jblotter.objects.component.Wire;
import com.github.falsepattern.jblotter.objects.component.pegs.Input;
import com.github.falsepattern.jblotter.objects.component.pegs.Output;
import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomSaveGenerator {
    private static String generateRandomString(Random random) {
        var text = new StringBuilder();
        int length = 4 + random.nextInt(16);
        for (int i = 0; i < length; i++) {
            text.append((char)(32 + random.nextInt(95)));
        }
        return text.toString();
    }

    public static BlotterFile generateSave(boolean world) {
        var random = new Random();
        var gameVersion = new Version(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE));
        var componentIDs = new String[16 + random.nextInt(64)];
        for (int i = 0; i < componentIDs.length; i++) componentIDs[i] = generateRandomString(random);
        var componentCount = 128 + random.nextInt(512);
        var components = new HashMap<Integer, Component>();
        var circuitStates = 16 + random.nextInt(65520);
        for (int i = 1; i < componentCount; i++) {
            short id = (short)random.nextInt(componentIDs.length);
            var inputs = new Input[random.nextInt(255)];
            for (int j = 0; j < inputs.length; j++) {
                inputs[j] = new Input(random.nextInt(circuitStates));
            }
            var outputs = new Output[random.nextInt(255)];
            for (int j = 0; j < outputs.length; j++) {
                outputs[j] = new Output(random.nextInt(circuitStates));
            }
            var customData = new byte[random.nextInt(128)];
            random.nextBytes(customData);
            components.put(i, new Component(i, world ? random.nextInt(i) : i == 1 ? 0 : 1 + random.nextInt(i - 1), id,
                    new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()),
                    new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat()),
                    inputs, outputs, customData));
        }
        var wires = new Wire[256 + random.nextInt(2048)];
        for (int i = 0; i < wires.length; i++) {
            var firstAddress = generatePegAddress(components, random);
            var secondAddress= generatePegAddress(components, random);
            wires[i] = new Wire(firstAddress, secondAddress, random.nextInt(circuitStates), random.nextFloat());
        }
        byte[] states = null;
        ArrayList<Integer> subassemblyCircuitStates = null;
        if (world) {
            states = new byte[circuitStates / 8];
            random.nextBytes(states);
        } else {
            subassemblyCircuitStates = new ArrayList<>();
            for (int i = 0; i < circuitStates; i++) {
                if (random.nextBoolean()) subassemblyCircuitStates.add(i);
            }
        }
        var mods = new HashMap<String, Version>();
        int modCount = random.nextInt(32);
        for (int i = 0; i < modCount; i++) {
            var name = generateRandomString(random);
            var version = new Version(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE));
            mods.put(name, version);
        }
        return new BlotterFile((byte)0x05, gameVersion, mods, world, componentIDs, components, wires, world ? circuitStates : subassemblyCircuitStates.size(), world ? BitSet.valueOf(states) : null, world ? null : subassemblyCircuitStates.stream().mapToInt(Integer::intValue).toArray());
    }

    private static PegAddress generatePegAddress(Map<Integer, Component> components, Random random) {
        while (true) {
            var input = random.nextBoolean();
            var component = components.get(1 + random.nextInt(components.size()));
            int peg;
            if (input) {
                if (component.inputs().length == 0) continue;
                peg = random.nextInt(component.inputs().length);
            } else {
                if (component.outputs().length == 0) continue;
                peg = random.nextInt(component.outputs().length);
            }
            return new PegAddress(input, component.address(), peg);
        }
    }
}
