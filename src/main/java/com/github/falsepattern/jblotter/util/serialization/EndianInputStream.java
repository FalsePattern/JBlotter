package com.github.falsepattern.jblotter.util.serialization;

import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndianInputStream extends FilterInputStream implements DataInput {
    private boolean littleEndian;
    public EndianInputStream(InputStream in, boolean littleEndian) {
        super(in);
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
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int bytesRead = readNBytes(b, off, len);
        if (bytesRead != len) throw new EOFException("Tried to read " + len + " bytes from stream, but only got " + bytesRead + " bytes before EOF!");

    }

    @Override
    public int skipBytes(int n) throws IOException {
        return (int)skip(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readUnsignedByte() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) readUnsignedByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int read = read();
        if (read == -1) throw new EOFException("Reached end of while while trying to read the next byte!");
        return read;
    }

    @Override
    public short readShort() throws IOException {
        return (short) readUnsignedShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        //noinspection IfStatementWithIdenticalBranches
        if (littleEndian) {
            return readUnsignedByte() | (readUnsignedByte() << 8);
        } else {
            return (readUnsignedByte() << 8) | readUnsignedByte();
        }
    }

    @Override
    public char readChar() throws IOException {
        return (char)((readUnsignedByte() << 8) | readUnsignedByte());
    }

    public int readInt() throws IOException {
        return littleEndian
                ?
                readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedByte() << 16) | (readUnsignedByte() << 24)
                :
                (readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | readUnsignedByte()
                ;
    }

    @Override
    public long readLong() throws IOException {
        return littleEndian
                ?
                ((long)readUnsignedByte()) | (((long)readUnsignedByte()) << 8) | (((long)readUnsignedByte()) << 16)
                | (((long)readUnsignedByte()) << 24) | (((long)readUnsignedByte()) << 32) | (((long)readUnsignedByte()) << 40)
                | (((long)readUnsignedByte()) << 48) | (((long)readUnsignedByte()) << 56)
                :
                (((long)readUnsignedByte()) << 56) | (((long)readUnsignedByte()) << 48) | (((long)readUnsignedByte()) << 40)
                | (((long)readUnsignedByte()) << 32) | (((long)readUnsignedByte()) << 24) | (((long)readUnsignedByte()) << 16)
                | (((long)readUnsignedByte()) << 8) | ((long)readUnsignedByte())
                ;
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() {
        throw new UnsupportedOperationException("Standard readLine is not implemented for Blot files!");
    }

    @Override
    public String readUTF() {
        throw new UnsupportedOperationException("Standard readUTF is not implemented for Blot files!");
    }


}
