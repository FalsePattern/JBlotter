package com.github.falsepattern.jblotter;

import com.github.falsepattern.jblotter.objects.BlotterFile;
import com.github.falsepattern.jblotter.objects.GameVersion;
import com.github.falsepattern.jblotter.objects.component.Component;
import com.github.falsepattern.jblotter.objects.component.Wire;
import com.github.falsepattern.jblotter.objects.component.pegs.Input;
import com.github.falsepattern.jblotter.objects.component.pegs.Output;
import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class RandomSaveGenerator {
    private static String generateRandomString(Random random) {
        StringBuilder text = new StringBuilder();
        int length = 4 + random.nextInt(16);
        for (int i = 0; i < length; i++) {
            text.append((char)(32 + random.nextInt(95)));
        }
        return text.toString();
    }

    public static BlotterFile generateSave(boolean world) {
        Random random = new Random();
        GameVersion gameVersion = new GameVersion(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE));
        String[] componentIDs = new String[16 + random.nextInt(64)];
        for (int i = 0; i < componentIDs.length; i++) componentIDs[i] = generateRandomString(random);
        Component[] components = new Component[128 + random.nextInt(512)];
        int circuitStates = 16 + random.nextInt(65520);
        for (int i = 1; i < components.length; i++) {
            short id = (short)random.nextInt(componentIDs.length);
            Input[] inputs = new Input[random.nextInt(255)];
            for (int j = 0; j < inputs.length; j++) {
                inputs[j] = new Input(random.nextBoolean(), random.nextInt(circuitStates));
            }
            Output[] outputs = new Output[random.nextInt(255)];
            for (int j = 0; j < outputs.length; j++) {
                outputs[j] = new Output(random.nextInt(circuitStates));
            }
            byte[] customData = new byte[random.nextInt(128)];
            random.nextBytes(customData);
            components[i] = new Component(i, world ? random.nextInt(i) : i == 1 ? 0 : 1 + random.nextInt(i - 1), id,
                    new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()),
                    new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat()),
                    inputs, outputs, customData);
        }
        Wire[] wires = new Wire[256 + random.nextInt(2048)];
        for (int i = 0; i < wires.length; i++) {
            PegAddress firstAddress = generatePegAddress(components, random);
            PegAddress secondAddress =generatePegAddress(components, random);
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
        return new BlotterFile((byte)0x05, gameVersion, world, componentIDs, components, wires, world ? circuitStates : subassemblyCircuitStates.size(), world ? BitSet.valueOf(states) : null, world ? null : subassemblyCircuitStates.stream().mapToInt(Integer::intValue).toArray());
    }

    private static PegAddress generatePegAddress(Component[] components, Random random) {
        while (true) {
            boolean input = random.nextBoolean();
            Component component = components[1 + random.nextInt(components.length - 1)];
            byte peg;
            if (input) {
                if (component.inputs().length == 0) continue;
                peg = (byte) random.nextInt(component.inputs().length);
            } else {
                if (component.outputs().length == 0) continue;
                peg = (byte) random.nextInt(component.outputs().length);
            }
            return new PegAddress(input, component.address(), peg);
        }
    }
}
