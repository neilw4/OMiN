package neilw4.omin.datastructure;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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
        assertTrue(cells.length >= (cellCount - 1) / Long.SIZE + 1);
        this.cells = cells.clone();
    }

    public static <T> BloomFilter<T> read(JsonReader reader) {
        try {
            reader.beginObject();
            assertEquals("cellCount", reader.nextName());
            int cellCount = reader.nextInt();
            assertEquals("hashes", reader.nextName());
            int hashes = reader.nextInt();
            assertEquals("cells", reader.nextName());
            reader.beginArray();
            long[] cells = new long[(cellCount - 1) / Long.SIZE + 1];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = reader.nextLong();
            }
            reader.endArray();
            reader.endObject();
            return new BloomFilter<>(cellCount, hashes, cells);
        } catch (IOException e) {
            // This error can't be ignored.
            throw new RuntimeException(e);
        }
    }

    public void write(JsonWriter writer) {
        try {
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
