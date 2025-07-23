import java.util.Arrays;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class UnionFind {

    /* DONE: Add instance variables here. */
    // private final int[] data;
    private final AtomicIntegerArray data;
    private final int N;
    private final Object lock = new Object(); // Lock for thread safety

    /* Creates a UnionFind data structure holding N items. Initially, all
     * items are in disjoint sets. */
    public UnionFind(int N) {
        // DONE: YOUR CODE HERE
        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0");
        }
        this.N = N;
        // data = new int[N];
        // Arrays.fill(data, -1);
        data = new AtomicIntegerArray(N);
        for (int i = 0; i < N; i++) {
            data.set(i, -1);
        }
    }

    /* Returns the size of the set V belongs to. */
    public int sizeOf(int v) {
        synchronized (lock) {
            return -data.get(find(v)); // return -data[find(v)];
        }
    }

    /* Returns the parent of V. If V is the root of a tree, returns the
     * negative size of the tree for which V is the root. */
    public int parent(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        return data.get(v); // return data[v];
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
        synchronized (lock) {
            return find(v1) == find(v2);
        }
    }

    /* Returns the root of the set V belongs to. Path-compression is employed
     * allowing for fast search-time. If invalid items are passed into this
     * function, throw an IllegalArgumentException. */
    public int find(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        int parent = data.get(v); // if (isRoot(v))
        if (parent < 0) {
            return v;
        }

        // synchronized (lock) {
        int root = v;
        // * Path compression with CAS
        while (data.get(root) >= 0) { // while (!isRoot(root))
            root = data.get(root); // root = parent(root);
        }
        // Compress path from v to root
        int cur = v;
        // for (int cur = v, par; !isRoot(cur); cur = par) {
        //     par = parent(cur);
        //     // if (par != root) data[cur] = root;
        //     if (par != root) data.set(cur, root);
        // }
        while (data.get(cur) >= 0) {
            int next = data.get(cur);
            data.compareAndSet(cur, next, root);
            cur = next;
        }
        return root;
        // }
    }

    /* Connects two items V1 and V2 together by connecting their respective sets.
     * V1 and V2 can be any element, and a union-by-size heuristic is used.
     * If the sizes of the sets are equal, tie-break by connecting V1's
     * root to V2's root. */
    public void union(int v1, int v2) {
        if (v1 < 0 || v1 >= N || v2 < 0 || v2 >= N) {
            throw new IllegalArgumentException("Invalid vertices: " + v1 + ", " + v2);
        }

        while (true) { // synchronized (lock)
            int root1 = find(v1);
            int root2 = find(v2);
            if (root1 == root2) {
                return;
            }
            int size1 = -data.get(root1);
            int size2 = -data.get(root2);
            if (sizeOf(root1) <= sizeOf(root2)) { // Try to make root1 child of root2
                // data.addAndGet(root2, data.get(root1)); // data[root2] += data[root1];
                // data.set(root1, root2); // data[root1] = root2;
                if (data.compareAndSet(root1, -size1, root2)) {
                    data.addAndGet(root2, -size1);
                    return;
                }
            } else { // Try to make root2 child of root1
                // data.addAndGet(root1, data.get(root2)); // data[root1] += data[root2];
                // data.set(root2, root1); // data[root2] = root1;
                if (data.compareAndSet(root2, -size2, root1)) {
                    data.addAndGet(root1, -size2);
                    return;
                }
            }
        } // If CAS failed, retry
    }

    private boolean isRoot(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        return data.get(v) < 0; // return data[v] < 0;
    }
}
