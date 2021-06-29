package com.github.falsepattern.jblotter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.falsepattern.jblotter.objects.component.pegs.PegAddress;
import com.github.falsepattern.jblotter.util.json.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SignalGraphSolver {
    private final Map<PegAddress, ObjectNode> addressMap = new HashMap<>();
    private final Map<ObjectNode, PegAddress> reverseMap = new HashMap<>();
    private final Map<PegAddress, Map<PegAddress, ObjectNode>> wires = new HashMap<>();
    private final Set<Integer> usedIDs = new HashSet<>();
    private final Map<Integer, Boolean> idStates = new HashMap<>();

    private void replaceNodeStateID(ObjectNode node, int old, int i) {
        if (node.has("circuitStateID") && node.get("circuitStateID").intValue() == old) {
            node.put("circuitStateID", i);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void defragIDs() {
        int max = usedIDs.stream().max(Integer::compareTo).get();
        for (int i = 0; i < max; i++) {
            if (!usedIDs.contains(i)) {
                usedIDs.add(i);
                usedIDs.remove(max);
                for (var node: addressMap.values()) {
                    replaceNodeStateID(node, max, i);
                }
                for (var group: wires.values()) {
                    for (var node: group.values()) {
                        replaceNodeStateID(node, max, i);
                    }
                }
                max = usedIDs.stream().max(Integer::compareTo).get();
            }
        }
    }

    private int nextFreeID() {
        for (int i = 0;;i++) {
            if (!usedIDs.contains(i)) return i;
        }
    }

    public void addComponents(ArrayNode components) {
        for (var component: components) {
            addPegs((ObjectNode) component);
        }
    }

    public void addPegs(ObjectNode component) {
        int componentAddress = (int)component.get("componentAddress").longValue();
        var inputs = (ArrayNode) component.get("inputs");
        var outputs = (ArrayNode) component.get("outputs");
        var inputCount = inputs.size();
        var outputCount = outputs.size();
        for (int i = 0; i < inputCount; i++) {
            var input = (ObjectNode)inputs.get(i);
            var addr = new PegAddress(true, componentAddress, (byte)i);
            addressMap.put(addr, input);
            reverseMap.put(input, addr);
        }
        for (int i = 0; i < outputCount; i++) {
            var output = (ObjectNode)outputs.get(i);
            var addr = new PegAddress(false, componentAddress, (byte)i);
            addressMap.put(addr, output);
            reverseMap.put(output, addr);
        }
    }

    public void addWires(ArrayNode wires) throws JsonParseException {
        for (var wire: wires) {
            var first = (ObjectNode)wire.get("firstPoint");
            var second = (ObjectNode)wire.get("secondPoint");
            var start = PegAddress.fromJson(first, true);
            var end = PegAddress.fromJson(second, true);
            this.wires.computeIfAbsent(start, (ignored) -> new HashMap<>()).put(end, (ObjectNode) wire);
            this.wires.computeIfAbsent(end, (ignored) -> new HashMap<>()).put(start, (ObjectNode) wire);
        }
    }

    private void solveNode(ArrayList<ObjectNode> scheduledNodes, ArrayList<ObjectNode> nodesToProcess) throws JsonParseException {
        var node = scheduledNodes.remove(0);
        var addr = reverseMap.get(node);
        var wires = this.wires.getOrDefault(addr, null);
        if (wires == null) {
            int id = nextFreeID();
            node.put("circuitStateID", id);
            usedIDs.add(id);
        } else if (!addr.input() || node.get("exclusive").booleanValue()){
            int id = nextFreeID();
            node.put("circuitStateID", id);
            usedIDs.add(id);
            if (!addr.input()) {
                for (var wire: wires.values()) {
                    wire.put("circuitStateID", id);
                }
                idStates.put(id, node.get("powered").asBoolean());
            }
        } else {
            int id = -1;
            if (node.has("circuitStateID")) {
                id = node.get("circuitStateID").intValue();
            } else {
                boolean hasID = false;
                for (var otherAndWire : wires.entrySet()) {
                    var otherAddr = otherAndWire.getKey();
                    var other = addressMap.get(otherAddr);
                    if (!otherAddr.input() || other.get("exclusive").booleanValue()) continue;
                    if (other.has("circuitStateID")) {
                        if (!hasID) {
                            hasID = true;
                            id = other.get("circuitStateID").intValue();
                        } else {
                            throw new JsonParseException("Id mapping collision!");
                        }
                    }
                }
                if (!hasID) {
                    id = nextFreeID();
                    usedIDs.add(id);
                    node.put("circuitStateID", id);
                }
            }
            for (var otherAndWire : wires.entrySet()) {
                var otherAddr = otherAndWire.getKey();
                var other = addressMap.get(otherAddr);
                if (!otherAddr.input()) {
                    if (other.get("powered").booleanValue()) {
                        idStates.put(id, true);
                    }
                } else if (!other.get("exclusive").booleanValue() && nodesToProcess.contains(other)) {
                    scheduledNodes.add(other);
                    nodesToProcess.remove(other);
                    other.put("circuitStateID", id);
                    otherAndWire.getValue().put("circuitStateID", id);
                }
            }
        }
    }

    private boolean propagateState() {
        for (var entry: addressMap.entrySet()) {
            var addr = entry.getKey();
            var node = entry.getValue();
            var csid = node.get("circuitStateID").intValue();
            if (idStates.getOrDefault(csid, false)) continue;
            for (var wire: wires.get(addr).values()) {
                if (idStates.getOrDefault(wire.get("circuitStateID").intValue(), false)) {
                    idStates.put(csid, true);
                    return true;
                }
            }
        }
        return false;
    }

    public void solve(ObjectNode blotterFile, boolean world) throws JsonParseException {
        var scheduledNodes = new ArrayList<ObjectNode>();
        var nodesToProcess = new ArrayList<>(addressMap.values());
        while (!nodesToProcess.isEmpty()) {
            scheduledNodes.add(nodesToProcess.remove(0));
            while (!scheduledNodes.isEmpty()) {
                solveNode(scheduledNodes, nodesToProcess);
            }
        }
        for (var node: addressMap.values()) {
            if (node.has("powered")) {
                node.remove("powered");
            }
        }
        if (world) {
            var arr = new ArrayNode(JsonNodeFactory.instance);
            @SuppressWarnings("OptionalGetWithoutIsPresent") int max = idStates.keySet().stream().max(Integer::compareTo).get();
            for (int i = 0; i <= max; i++) {
                arr.add(idStates.containsKey(i) && idStates.get(i));
            }
            blotterFile.set("circuitStates", arr);
        } else {
            var fields = idStates.keySet().stream().sorted().toList();
            var arr = new ArrayNode(JsonNodeFactory.instance);
            for (var field: fields) {
                arr.add(field);
            }
            blotterFile.set("circuitStates", arr);
        }

        //noinspection StatementWithEmptyBody
        while (propagateState()){}
    }
}
