package com.github.falsepattern.jblotter.util;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EndianOutputStream extends FilterOutputStream implements DataOutput {
    private boolean littleEndian;
    public EndianOutputStream(OutputStream out, boolean littleEndian) {
        super(out);
        this.littleEndian = littleEndian;
    }


    public void setEndianness(boolean littleEndian) {
        this.littleEndian = littleEndian;
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public boolean isBigEndian() {
        return !littleEndian;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 0x01 : 0x00);
    }

    @Override
    public void writeByte(int v) throws IOException {
        write(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        if (littleEndian) {
            write(v & 0xff);
            write ((v >> 8) & 0xff);
        } else {
            write ((v >> 8) & 0xff);
            write(v & 0xff);
        }
    }

    @Override
    public void writeChar(int v) throws IOException {
        if (littleEndian) {
            write(v & 0xff);
            write ((v >> 8) & 0xff);
        } else {
            write ((v >> 8) & 0xff);
            write(v & 0xff);
        }
    }

    @Override
    public void writeInt(int v) throws IOException {
        if (littleEndian) {
            write(v & 0xff);
            write ((v >> 8) & 0xff);
            write ((v >> 16) & 0xff);
            write ((v >> 24) & 0xff);
        } else {
            write ((v >> 24) & 0xff);
            write ((v >> 16) & 0xff);
            write ((v >> 8) & 0xff);
            write(v & 0xff);
        }
    }

    @Override
    public void writeLong(long v) throws IOException {
        if (littleEndian) {
            write((int) (v & 0xff));
            write ((int) ((v >> 8) & 0xff));
            write ((int) ((v >> 16) & 0xff));
            write ((int) ((v >> 24) & 0xff));
            write ((int) ((v >> 32) & 0xff));
            write ((int) ((v >> 40) & 0xff));
            write ((int) ((v >> 48) & 0xff));
            write ((int) ((v >> 56) & 0xff));
        } else {
            write ((int) ((v >> 56) & 0xff));
            write ((int) ((v >> 48) & 0xff));
            write ((int) ((v >> 40) & 0xff));
            write ((int) ((v >> 32) & 0xff));
            write ((int) ((v >> 24) & 0xff));
            write ((int) ((v >> 16) & 0xff));
            write ((int) ((v >> 8) & 0xff));
            write((int) (v & 0xff));
        }
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(String s) {
        throw new UnsupportedOperationException("Standard writeBytes is not implemented for Blot files!");
    }

    @Override
    public void writeChars(String s) {
        throw new UnsupportedOperationException("Standard writeChars is not implemented for Blot files!");
    }

    @Override
    public void writeUTF(String s) {
        throw new UnsupportedOperationException("Standard writeUTF is not implemented for Blot files!");
    }
}
