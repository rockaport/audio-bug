package com.asesolutions.mobile.wav.models;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class FmtSubChunk {
    private static final int numBytes = 4 + 3 * 4 + 4 * 2;
    private static final int offset = 12;
    private static byte[] chunkId = {'f', 'm', 't', ' '};
    private static int chunkSize = 16;
    private short audioFormat;
    private short numChannels;
    private int sampleRate;
    private int byteRate;
    private short blockAlign;
    private short bitsPerSample;

    public FmtSubChunk(int audioFormat, int numChannels, int sampleRate, int bitsPerSample) {
        this.audioFormat = (short) audioFormat;
        this.numChannels = (short) numChannels;
        this.sampleRate = sampleRate;
        this.bitsPerSample = (short) bitsPerSample;

        byteRate = sampleRate * numChannels * bitsPerSample / 8;
        blockAlign = (short) (numChannels * bitsPerSample / 8);
    }

    public static FmtSubChunk getChunk(RandomAccessFile randomAccessFile) throws IOException {
        byte[] buffer = new byte[4];

        // Read chunk ID
        randomAccessFile.seek(offset);
        randomAccessFile.read(buffer);
        if (!Arrays.equals(buffer, chunkId)) {
            String errorMessage = String.format(
                    "Chunk ID does not match: %s %s ",
                    new String(chunkId),
                    new String(buffer));
            throw new IOException(errorMessage);
        }

        // Read chunk size
        randomAccessFile.read(buffer);
        int tempChunkSize = Integer.reverseBytes(ByteBuffer.wrap(buffer).getInt());
        if (tempChunkSize != chunkSize) {
            String errorMessage = String.format(
                    "Chunk size does not match: %d %d ",
                    chunkSize,
                    tempChunkSize);
            throw new IOException(errorMessage);
        }

        // Read audio format
        randomAccessFile.read(buffer, 0, 2);
        short audioFormat = Short.reverseBytes(ByteBuffer.wrap(buffer, 0, 2).getShort());
        // Read audio format
        randomAccessFile.read(buffer, 0, 2);
        short numChannels = Short.reverseBytes(ByteBuffer.wrap(buffer, 0, 2).getShort());
        // Read audio format
        randomAccessFile.read(buffer, 0, 4);
        int sampleRate = Integer.reverseBytes(ByteBuffer.wrap(buffer).getInt());
        // Read audio format
        randomAccessFile.read(buffer, 0, 4);
        int byteRate = Integer.reverseBytes(ByteBuffer.wrap(buffer).getInt());
        // Read audio format
        randomAccessFile.read(buffer, 0, 2);
        short blockAlign = Short.reverseBytes(ByteBuffer.wrap(buffer, 0, 2).getShort());
        // Read audio format
        randomAccessFile.read(buffer, 0, 2);
        short bitsPerSample = Short.reverseBytes(ByteBuffer.wrap(buffer, 0, 2).getShort());

        // Create the subchunk based on read data
        FmtSubChunk fmtSubChunk = new FmtSubChunk(
                audioFormat,
                numChannels,
                sampleRate,
                bitsPerSample);

        // Test if the derived and read byte rate does not match
        if (fmtSubChunk.getByteRate() != byteRate) {
            String errorMessage = String.format(
                    "Byte rate does not match: %d %d ",
                    fmtSubChunk.getByteRate(),
                    byteRate);
            throw new IOException(errorMessage);
        }

        // Test if the derived and read block alignment does not match
        if (fmtSubChunk.getBlockAlign() != blockAlign) {
            String errorMessage = String.format(
                    "Block align does not match: %d %d ",
                    fmtSubChunk.getBlockAlign(),
                    blockAlign);
            throw new IOException(errorMessage);
        }

        return fmtSubChunk;
    }

    public void writeBytes(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.seek(offset);
        randomAccessFile.write(getBytes());
    }

    public byte[] getBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(numBytes)
                .put(chunkId)
                .putInt(Integer.reverseBytes(chunkSize))
                .putShort(Short.reverseBytes(audioFormat))
                .putShort(Short.reverseBytes(numChannels))
                .putInt(Integer.reverseBytes(sampleRate))
                .putInt(Integer.reverseBytes(byteRate))
                .putShort(Short.reverseBytes(blockAlign))
                .putShort(Short.reverseBytes(bitsPerSample));

        return byteBuffer.array();
    }

    public static int getChunkSize() {
        return chunkSize;
    }

    public short getAudioFormat() {
        return audioFormat;
    }

    public short getNumChannels() {
        return numChannels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getByteRate() {
        return byteRate;
    }

    public short getBlockAlign() {
        return blockAlign;
    }

    public short getBitsPerSample() {
        return bitsPerSample;
    }

//    @Override
//    public String toString() {
//        String jsonString = "json-error";
//        try {
//            jsonString = new JSONStringer()
//                    .object()
//                    .key("chunkId").value(chunkId)
//                    .key("chunkSize").value(chunkSize)
//                    .key("audioFormat").value(audioFormat)
//                    .key("numChannels").value(numChannels)
//                    .key("sampleRate").value(sampleRate)
//                    .key("byteRate").value(byteRate)
//                    .key("blockAlign").value(blockAlign)
//                    .key("bitsPerSample").value(bitsPerSample)
//                    .endObject()
//                    .toString();
//        } catch (JSONException e) {
//        }
//
//        return getClass().getSimpleName() + jsonString;
//    }
}
