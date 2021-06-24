# JBlotter
A Java implementation of the [Blotter file format 5 Draft 0](https://gist.github.com/JimmyCushnie/7dae698c92a900f2e530668740c20ca2#int).

The goal of this implementation is to provide a well tested, and easy to use serialization and deserialization library 
for [Logic World](https://logicworld.net/) 's file format.

### Disclaimer
This is an unofficial project developed independent of the official implementation.


### Using the (de)serializer:
You can use the BlotterFile.deserialize() method to deserialize from a given input stream,
and the blotterFile.serialize() method to serialize it.

Because Java's DataInputStream and DataOutputStream is big-endian, while the blotter file format is little endian, you will need
to use the EndianInputStream and EndianOutputStream replacements in little-endian mode.
