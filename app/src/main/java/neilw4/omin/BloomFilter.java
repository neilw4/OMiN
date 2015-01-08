package neilw4.omin;

import java.util.List;

public class BloomFilter<T> {

    private final long[] cells;
    private final int hashes;

    public BloomFilter(int cellCount, int hashes) {
        cells = new long[cellCount / Long.SIZE];
        this.hashes = hashes;
    }

    public boolean put(T t) {
        boolean exists = true;
        for (int i = 0; i < hashes; i++) {
            int hash = hash(t, i);
            int cell = hash / Long.SIZE;
            int mask = 0x1 << (hash % Long.SIZE);
            if ((cells[cell] & mask) == 0) {
                exists = false;
            }
            cells[cell] &= mask;
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

    private int hash(T t, int n) {
        int hash = (t.hashCode() >> ((n * Integer.SIZE) / hashes));
        if (hash < 0) {
            hash *= -1;
        }
        return hash % (cells.length * Long.SIZE);
    }

}
