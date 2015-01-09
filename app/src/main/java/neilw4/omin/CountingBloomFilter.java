package neilw4.omin;

public class CountingBloomFilter<T> {

    private final int[] cells;
    private final int hashes;
    private final int cellVals;

    public CountingBloomFilter(int cellCount, int hashes, int cellVals) {
        cells = new int[cellCount];
        this.hashes = hashes;
        this.cellVals = cellVals;
    }

    public CountingBloomFilter(int cells, int hashes) {
        this(cells, hashes, 2);
    }

    public boolean put(T t) {
        boolean exists = true;
        for (int i = 0; i < hashes; i++) {
            int hash = hash(t, i);
            if (cells[hash] == 0) {
                exists = false;
            }
            cells[hash] = cellVals - 1;
        }
        return exists;
    }

    public boolean mayContain(T t) {
        for (int i = 0; i < hashes; i++) {
            int hash = hash(t, i);
            if (cells[hash] == 0) {
                return false;
            }
        }
        return true;
    }

    public BloomFilter<T> asBinaryFilter() {
        BloomFilter<T> filter = new BloomFilter<>(cells.length, hashes);
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != 0) {
                filter.setCells(i);
            }
        }
        return filter;
    }

    public void reduce() {
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != 0) {
                cells[i]--;
            }
        }
    }

    private int hash(T t, int n) {
        int hash = (t.hashCode() >> ((n * Integer.SIZE) / hashes));
        if (hash < 0) {
            hash *= -1;
        }
        return hash % cells.length;
    }

}
