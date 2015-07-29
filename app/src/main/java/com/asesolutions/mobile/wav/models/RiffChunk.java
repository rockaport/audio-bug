package com.asesolutions.mobile.wav.models;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RiffChunk {
    private static final int numBytes = 2 * 4 + 4;
    private static final int offset = 0;
    private static byte[] chunkId = {'R', 'I', 'F', 'F'};
    private int chunkSize;
    private static byte[] format = {'W', 'A', 'V', 'E'};

    public void updateChunkSize(int dataSubChunkSize) {
        chunkSize = 36 + dataSubChunkSize;
    }

    public static RiffChunk getChunk(RandomAccessFile randomAccessFile) throws IOException {
        byte[] buffer = new byte[4];
        RiffChunk riffChunk = new RiffChunk();

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
        riffChunk.updateChunkSize(Integer.reverseBytes(ByteBuffer.wrap(buffer).getInt()));

        // Read format
        randomAccessFile.read(buffer);
        if (!Arrays.equals(buffer, format)) {
            String errorMessage = String.format(
                    "Chunk ID does not match: %s %s ",
                    new String(format),
                    new String(buffer));
            throw new IOException(errorMessage);
        }

        return riffChunk;
    }

    public byte[] getBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(numBytes)
                .put(chunkId)
                .putInt(Integer.reverseBytes(chunkSize))
                .put(format);

        return byteBuffer.array();
    }

    public void writeBytes(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.seek(offset);
        randomAccessFile.write(getBytes());
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer()
                .append(RiffChunk.class.getSimpleName()).append(" {\n")
                .append("chunkId: " + new String(chunkId)).append("\n")
                .append("chunkSize: " + chunkSize).append("\n")
                .append("chunkId: " + new String(format)).append("\n")
                .append("}\n");

        return stringBuffer.toString();
    }
}
