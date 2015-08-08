package com.asesolutions.mobile.wav.models;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataSubChunk {
    private static byte[] chunkId = {'d', 'a', 't', 'a'};
    private static final int numBytes = 8; // 4 + 4
    private static final int offset = 36;
    private int chunkSize;

    public DataSubChunk(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public static DataSubChunk getChunk(RandomAccessFile randomAccessFile) throws IOException {
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
        randomAccessFile.read(buffer, 0, 4);
        int chunkSize = Integer.reverseBytes(ByteBuffer.wrap(buffer).getInt());

        return new DataSubChunk(chunkSize);
    }

    public byte[] getBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(numBytes)
                .put(chunkId)
                .putInt(Integer.reverseBytes(chunkSize));

        return byteBuffer.array();
    }

    public void writeBytes(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.seek(offset);
        randomAccessFile.write(getBytes());
    }

    public void updateChunkSize(int chunkSize) {
        this.chunkSize += chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(DataSubChunk.class.getSimpleName()).append(" {\n")
                .append("    chunkId: " + new String(chunkId)).append("\n")
                .append("    chunkSize: " + chunkSize).append("\n")
                .append("}\n");

        return stringBuilder.toString();
    }
}
