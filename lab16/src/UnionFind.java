import java.util.Arrays;

public class UnionFind {

    /* DONE: Add instance variables here. */
    private final int[] data;
    private final int N;

    /* Creates a UnionFind data structure holding N items. Initially, all
     * items are in disjoint sets. */
    public UnionFind(int N) {
        // DONE: YOUR CODE HERE
        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0");
        }
        this.N = N;
        data = new int[N];
        Arrays.fill(data, -1);
    }

    /* Returns the size of the set V belongs to. */
    public int sizeOf(int v) {
        // DONE: YOUR CODE HERE
        return -data[find(v)]; // Return the size of the set by accessing the root's value
    }

    /* Returns the parent of V. If V is the root of a tree, returns the
     * negative size of the tree for which V is the root. */
    public int parent(int v) {
        // DONE: YOUR CODE HERE
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        // if (isRoot(v)) {
        //     return data[v]; // Return negative size if v is root
        // }
        // // * Path compression
        // while (parent(v) >= 0) {
        //     data[v] = parent(parent(v)); // Recursively find the root
        // }
        return data[v];
    }

    /** Returns true if nodes V1 and V2 are connected.
     * <p>
     * This method is pure: it does not modify any state.
     */
    public boolean connected(int v1, int v2) {
        // DONE: YOUR CODE HERE
        if (v1 < 0 || v1 >= N || v2 < 0 || v2 >= N) {
            throw new IllegalArgumentException("Invalid vertices: " + v1 + ", " + v2);
        }
        if (v1 == v2) {
            return true; // If both vertices are the same, they are trivially connected
        }
        return find(v1) == find(v2);
    }

    /* Returns the root of the set V belongs to. Path-compression is employed
     * allowing for fast search-time. If invalid items are passed into this
     * function, throw an IllegalArgumentException. */
    public int find(int v) {
        // DONE: YOUR CODE HERE
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        if (isRoot(v)) {
            return v; // If v is the root, return it
        }
        // // * recursive path compression
        // data[v] = find(data[v]);
        // return data[v];
        // * iterative path compression
        int root = v;
        while (!isRoot(root)) {
            root = parent(root);
        }
        for (int cur=v, par; !isRoot(cur); cur = par) {
            par = parent(cur);
            if (par != root) data[cur] = root;
        }
        return root;
    }

    /* Connects two items V1 and V2 together by connecting their respective sets.
     * V1 and V2 can be any element, and a union-by-size heuristic is used.
     * If the sizes of the sets are equal, tie-break by connecting V1's
     * root to V2's root. */
    public void union(int v1, int v2) {
        // DONE: YOUR CODE HERE
        if (v1 < 0 || v1 >= N || v2 < 0 || v2 >= N) {
            throw new IllegalArgumentException("Invalid vertices: " + v1 + ", " + v2);
        }
        int root1 = find(v1);
        int root2 = find(v2);
        if (root1 == root2) {
            return; // They are already connected
        }
        int size1 = sizeOf(root1); // Size of the set rooted at root1
        int size2 = sizeOf(root2); // Size of the set rooted at root2
        if (size1 <= size2) {
            data[root2] += data[root1]; // Merge root1 into root2
            data[root1] = root2; // Set root1's parent to root2
        } else {
            data[root1] += data[root2]; // Merge root2 into root1
            data[root2] = root1; // Set root2's parent to root1
        }
    }

    private boolean isRoot(int v) {
        if (v < 0 || v >= N) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
        return data[v] < 0;
    }
}
