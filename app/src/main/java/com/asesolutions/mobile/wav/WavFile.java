package com.asesolutions.mobile.wav;

import com.asesolutions.mobile.wav.models.DataSubChunk;
import com.asesolutions.mobile.wav.models.FmtSubChunk;
import com.asesolutions.mobile.wav.models.RiffChunk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class WavFile {
    private RiffChunk riffChunk;
    private FmtSubChunk fmtSubChunk;
    private DataSubChunk dataSubChunk;

    public static WavFile readFile(RandomAccessFile randomAccessFile) throws IOException {
        WavFile wavFile = new WavFile();

        wavFile.setRiffChunk(RiffChunk.getChunk(randomAccessFile));
        wavFile.setFmtSubChunk(FmtSubChunk.getChunk(randomAccessFile));
        wavFile.setDataSubChunk(DataSubChunk.getChunk(randomAccessFile));

        return wavFile;
    }

    public void writeFile(RandomAccessFile randomAccessFile) throws IOException {
        riffChunk.writeBytes(randomAccessFile);
        fmtSubChunk.writeBytes(randomAccessFile);
        dataSubChunk.writeBytes(randomAccessFile);
    }

    public void writeData(RandomAccessFile randomAccessFile, byte[] bytes) throws IOException {
        randomAccessFile.seek(randomAccessFile.length());

        int bytesPerSample = fmtSubChunk.getBitsPerSample() / 8;
        if (bytesPerSample == 1) {
            randomAccessFile.write(bytes);
        } else if (bytesPerSample == 2) {
            writeShorts(randomAccessFile, bytes);
        } else if (bytesPerSample == 4) {
            // WAVE_FORMAT_PCM
            if (fmtSubChunk.getAudioFormat() == 0x01) {
                writeInts(randomAccessFile, bytes);
            }
            // WAVE_FORMAT_IEEE_FLOAT –1.0 <= y <= +1.0
            else if (fmtSubChunk.getAudioFormat() == 0x03) {
                writeFloats(randomAccessFile, bytes);
            }
        }

        // Update chunk sizes
        int numSamples = bytes.length / bytesPerSample;
        int additionalSize = numSamples * fmtSubChunk.getNumChannels() * bytesPerSample;
        dataSubChunk.updateChunkSize(additionalSize);
        riffChunk.updateChunkSize(dataSubChunk.getChunkSize());

        // Write out chunk sizes
        riffChunk.writeBytes(randomAccessFile);
        dataSubChunk.writeBytes(randomAccessFile);
    }

    private void writeShorts(RandomAccessFile randomAccessFile, byte[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i += 2) {
            randomAccessFile.writeShort(Short.reverseBytes(ByteBuffer.wrap(bytes, i, 2).getShort()));
        }
    }

    private void writeInts(RandomAccessFile randomAccessFile, byte[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i += 4) {
            randomAccessFile.writeInt(Integer.reverseBytes(ByteBuffer.wrap(bytes, i, 4).getInt()));
        }
    }

    private void writeFloats(RandomAccessFile randomAccessFile, byte[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i += 4) {
            randomAccessFile.writeFloat(ByteBuffer.wrap(bytes, i, 4).getFloat());
        }
    }

    public RiffChunk getRiffChunk() {
        return riffChunk;
    }

    public void setRiffChunk(RiffChunk riffChunk) {
        this.riffChunk = riffChunk;
    }

    public FmtSubChunk getFmtSubChunk() {
        return fmtSubChunk;
    }

    public void setFmtSubChunk(FmtSubChunk fmtSubChunk) {
        this.fmtSubChunk = fmtSubChunk;
    }

    public DataSubChunk getDataSubChunk() {
        return dataSubChunk;
    }

    public void setDataSubChunk(DataSubChunk dataSubChunk) {
        this.dataSubChunk = dataSubChunk;
    }

    public static void main(String[] args) throws IOException {
        int sampleRate = 8000;
        int seconds = 5;
        int bytesPerSample = 1;
        int bitsPerSample = 8*bytesPerSample;
        int numSamples = sampleRate * seconds * bytesPerSample;

        RiffChunk riffChunk = new RiffChunk();
        FmtSubChunk fmtSubChunk = new FmtSubChunk(1, 1, sampleRate, bitsPerSample);
        DataSubChunk dataSubChunk = new DataSubChunk(0);

        File file = new File("test.wav");
        if (file.exists()) {
            file.delete();
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        WavFile wavFile = new WavFile();
        wavFile.setRiffChunk(riffChunk);
        wavFile.setFmtSubChunk(fmtSubChunk);
        wavFile.setDataSubChunk(dataSubChunk);
        wavFile.writeFile(randomAccessFile);


        byte[] samples = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            samples[i] = (byte) (255*Math.cos(2*Math.PI*2000/sampleRate*i));
        }
        wavFile.writeData(randomAccessFile, samples);
        randomAccessFile.close();


        randomAccessFile = new RandomAccessFile(file, "rw");
        wavFile = WavFile.readFile(randomAccessFile);
        wavFile.fmtSubChunk.toString();
    }
}
