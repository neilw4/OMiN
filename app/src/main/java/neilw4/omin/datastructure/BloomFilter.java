package neilw4.omin.datastructure;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertEquals;

public class BloomFilter<T> {

    private final int cellCount;
    private final long[] cells;
    private final int hashes;

    public BloomFilter(int cellCount, int hashes) {
        this(cellCount, hashes, new long[(cellCount - 1) / Long.SIZE + 1]);

        // Randomise initial values for obfuscation.
        Random r = new SecureRandom();
        for (int i = 0; i < cellCount; i++) {
            if (r.nextBoolean()) {
                setCells(i);
            }
        }
    }

    private BloomFilter(int cellCount, int hashes, long[] cells) {
        this.cellCount = cellCount;
        this.hashes = hashes;
        this.cells = cells.clone();
    }

    public BloomFilter(String str) {
        StringReader stringReader = new StringReader(str);
        try {
            JsonReader reader = new JsonReader(stringReader);
            reader.setLenient(false);

            reader.beginObject();
            assertEquals("cellCount", reader.nextName());
            cellCount = reader.nextInt();
            assertEquals("hashes", reader.nextName());
            hashes = reader.nextInt();
            assertEquals("cells", reader.nextName());
            reader.beginArray();
            cells = new long[(cellCount - 1) / Long.SIZE + 1];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = reader.nextLong();
            }
            reader.endArray();
            reader.endObject();

            reader.close();
            stringReader.close();
        } catch (IOException e) {
            // This error can't be ignored.
            throw new RuntimeException(e);
        }

    }

    @Override
    public String toString() {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.setLenient(false);
            writer.beginObject();
            writer.name("cellCount").value(cellCount);
            writer.name("hashes").value(hashes);
            writer.name("cells");
            writer.beginArray();
            for (long cell : cells) {
                writer.value(cell);
            }
            writer.endArray();
            writer.endObject();
            writer.close();
            return stringWriter.toString();
        } catch (IOException e) {
            // This error can't be ignored.
            throw new RuntimeException(e);
        }
    }

    public boolean put(T t) {
        boolean exists = true;
        for (int i = 0; i < hashes; i++) {
            int hash = hash(t, i);
            if (!setCells(hash)) {
                exists = false;
            }
        }
        return exists;
    }

    public void put(List<T> ts) {
        for (T t: ts) {
            put(t);
        }
    }

    public boolean mayContain(T t) {
        for (int i = 0; i < hashes; i++) {
            int hash = hash(t, i);
            int cell = hash / Long.SIZE;
            int mask = 0x1 << (hash % Long.SIZE);
            if ((cells[cell] & mask) == 0) {
                return false;
            }
        }
        return true;
    }

    protected boolean setCells(int hash) {
        int cell = hash / Long.SIZE;
        int mask = 0x1 << (hash % Long.SIZE);
        boolean exists = (cells[cell] & mask) != 0;
        cells[cell] &= mask;
        return exists;

    }

    private int hash(T t, int n) {
        int hash = (t.hashCode() >> ((n * Integer.SIZE) / hashes));
        if (hash < 0) {
            hash *= -1;
        }
        return hash % cellCount;
    }

}
