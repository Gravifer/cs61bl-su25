import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class UnionFind {

    /* DONE: Add instance variables here. */
    // private final int[] data;
    private final int[] data;
    private final int N;
    private final ReentrantLock[] locks;

    /* Creates a UnionFind data structure holding N items. Initially, all
     * items are in disjoint sets. */
    public UnionFind(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0");
        }
        this.N = N;
        data = new int[N];
        Arrays.fill(data, -1);
        locks = new ReentrantLock[N];
        for (int i = 0; i < N; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /* Returns the size of the set V belongs to. */
    public int sizeOf(int v) {
        return -data[find(v)];
    }

    /* Returns the parent of V. If V is the root of a tree, returns the
     * negative size of the tree for which V is the root. */
    public int parent(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        return data[v];
    }

    /** Returns true if nodes V1 and V2 are connected.
     * <p>
     * This method is pure: it does not modify any state.
     */
    public boolean connected(int v1, int v2) {
        if (v1 < 0 || v1 >= N || v2 < 0 || v2 >= N) {
            throw new IllegalArgumentException("Invalid vertices: " + v1 + ", " + v2);
        }
        if (v1 == v2) {
            return true;
        }
        return find(v1) == find(v2);
    }

    /* Returns the root of the set V belongs to. Path-compression is employed
     * allowing for fast search-time. If invalid items are passed into this
     * function, throw an IllegalArgumentException. */
    public int find(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        int cur = v;
        while (true) {
            int parent = data[cur];
            if (parent < 0) {
                return cur;
            }
            int grandparent = data[parent];
            if (grandparent < 0) {
                return parent;
            }
            // Path halving: set cur's parent to its grandparent
            if (data[cur] == parent) {
                data[cur] = grandparent;
            }
            cur = parent;
        }
    }

    public void union(int v1, int v2) {
        if (v1 < 0 || v1 >= N || v2 < 0 || v2 >= N) {
            throw new IllegalArgumentException("Invalid vertices: " + v1 + ", " + v2);
        }
        while (true) {
            int root1 = find(v1);
            int root2 = find(v2);
            if (root1 == root2) {
                return;
            }
            int min = Math.min(root1, root2);
            int max = Math.max(root1, root2);
            locks[min].lock();
            locks[max].lock();
            try {
                root1 = find(root1);
                root2 = find(root2);
                if (root1 == root2) {
                    return;
                }
                int size1 = -data[root1];
                int size2 = -data[root2];
                if (size1 <= size2) {
                    data[root1] = root2;
                    data[root2] = -(size1 + size2);
                } else {
                    data[root2] = root1;
                    data[root1] = -(size1 + size2);
                }
                return;
            } finally {
                locks[max].unlock();
                locks[min].unlock();
            }
        }
    }

    private boolean isRoot(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        return data[v] < 0;
    }
}
