import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.util.*;

/* A mutable and finite Graph object. Edge labels are stored via a HashMap
   where labels are mapped to a key calculated by the following. The graph is
   undirected (whenever an Edge is added, the dual Edge is also added). Vertices
   are numbered starting from 0. */
public class Graph {

    /* Maps vertices to a list of its neighboring vertices. */
    private HashMap<Integer, Set<Integer>> neighbors = new HashMap<>();
    /* Maps vertices to a list of its connected edges. */
    private HashMap<Integer, Set<Edge>> edges = new HashMap<>();
    /* A sorted set of all edges. */
    private TreeSet<Edge> allEdges = new TreeSet<>();

    /* Returns the vertices that neighbor V. */
    public TreeSet<Integer> getNeighbors(int v) {
        return new TreeSet<Integer>(neighbors.get(v));
    }

    /* Returns all edges adjacent to V. */
    public TreeSet<Edge> getEdges(int v) {
        return new TreeSet<Edge>(edges.get(v));
    }

    /* Returns a sorted list of all vertices. */
    public TreeSet<Integer> getAllVertices() {
        return new TreeSet<Integer>(neighbors.keySet());
    }

    /* Returns a sorted list of all edges. */
    public TreeSet<Edge> getAllEdges() {
        return new TreeSet<Edge>(allEdges);
    }

    /* Adds vertex V to the graph. */
    public void addVertex(Integer v) {
        if (neighbors.get(v) == null) {
            neighbors.put(v, new HashSet<Integer>());
            edges.put(v, new HashSet<Edge>());
        }
    }

    /* Adds Edge E to the graph. */
    public void addEdge(Edge e) {
        addEdgeHelper(e.getSource(), e.getDest(), e.getWeight());
    }

    /* Creates an Edge between V1 and V2 with no weight. */
    public void addEdge(int v1, int v2) {
        addEdgeHelper(v1, v2, 0);
    }

    /* Creates an Edge between V1 and V2 with weight WEIGHT. */
    public void addEdge(int v1, int v2, int weight) {
        addEdgeHelper(v1, v2, weight);
    }

    /* Returns true if V1 and V2 are connected by an edge. */
    public boolean isNeighbor(int v1, int v2) {
        return neighbors.get(v1).contains(v2) && neighbors.get(v2).contains(v1);
    }

    /* Returns true if the graph contains V as a vertex. */
    public boolean containsVertex(int v) {
        return neighbors.get(v) != null;
    }

    /* Returns true if the graph contains the edge E. */
    public boolean containsEdge(Edge e) {
        return allEdges.contains(e);
    }

    /* Returns if this graph spans G. */
    public boolean spans(Graph g) {
        TreeSet<Integer> all = getAllVertices();
        if (all.size() != g.getAllVertices().size()) {
            return false;
        }
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> vertices = new ArrayDeque<>();
        Integer curr;

        vertices.add(all.first());
        while ((curr = vertices.poll()) != null) {
            if (!visited.contains(curr)) {
                visited.add(curr);
                for (int n : getNeighbors(curr)) {
                    vertices.add(n);
                }
            }
        }
        return visited.size() == g.getAllVertices().size();
    }

    /* Overrides objects equals method. */
    public boolean equals(Object o) {
        if (!(o instanceof Graph)) {
            return false;
        }
        Graph other = (Graph) o;
        return neighbors.equals(other.neighbors) && edges.equals(other.edges);
    }

    /* A helper function that adds a new edge from V1 to V2 with WEIGHT as the
       label. */
    private void addEdgeHelper(int v1, int v2, int weight) {
        addVertex(v1);
        addVertex(v2);

        neighbors.get(v1).add(v2);
        neighbors.get(v2).add(v1);

        Edge e1 = new Edge(v1, v2, weight);
        Edge e2 = new Edge(v2, v1, weight);
        edges.get(v1).add(e1);
        edges.get(v2).add(e2);
        allEdges.add(e1);
    }

    public Graph prims(int start) {
        // TODO: YOUR CODE HERE
        if (!containsVertex(start)) {
            throw new IllegalArgumentException("Start vertex not in graph.");
        }
        Graph mst = new Graph();
        Set<Integer> visited = new HashSet<>();
        PrimVertexComparator pvc = new PrimVertexComparator(new HashMap<>());
        var distFromTree = pvc.distFromTree;
        PriorityQueue<Integer> pq = new PriorityQueue<>(pvc);
        for (Integer v : getAllVertices()) {
            if (v == start) {
                distFromTree.put(v, new Edge(-1, v, 0)); // Start vertex has no incoming edge
            } else {
                distFromTree.put(v, new Edge(-1, v, Integer.MAX_VALUE)); // Initialize with max weight
            }
            pq.add(v);
        }
        while (!pq.isEmpty()) {
            Integer current = pq.poll();
            if (visited.contains(current)) {
                continue; // Skip if already visited
            }
            visited.add(current);
            mst.addVertex(current);
            Edge edgeToAdd = distFromTree.get(current);
            if (edgeToAdd.getSource() != -1) { // Not the start vertex
                mst.addEdge(edgeToAdd);
            }

            for (Integer neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    Edge edge = new Edge(current, neighbor, getEdges(current).stream()
                            .filter(e -> e.getDest() == neighbor || e.getSource() == neighbor)
                            .findFirst().orElseThrow().getWeight());
                    if (edge.getWeight() < distFromTree.get(neighbor).getWeight()) {
                        distFromTree.put(neighbor, edge);
                        pq.remove(neighbor); // Remove to update priority queue
                        pq.add(neighbor); // Re-add with updated weight
                    }
                }
            }
        }
        // Add edges to the MST
        for (Edge e : distFromTree.values()) {
            if (e.getSource() != -1 && e.getWeight() < Integer.MAX_VALUE) {
                mst.addEdge(e);
            }
        }
        // Check if the MST spans all vertices
        if (!mst.spans(this)) {
            return null;
            // throw new IllegalStateException("MST does not span the graph.");
        }
        return mst;
    }

    public Graph kruskals() {
        // DONE: YOUR CODE HERE
        var uf = new WeightedQuickUnionUF(getAllVertices().size());
        Graph mst = new Graph();
        for (Edge edge : getAllEdges()) {
            int root1 = uf.find(edge.getSource());
            int root2 = uf.find(edge.getDest());
            if (root1 != root2) {
                uf.union(root1, root2);
                mst.addEdge(edge);
            }
        }
        // Check if the MST spans all vertices
        if (!mst.spans(this)) {
            // throw new IllegalStateException("MST does not span the graph.");
            return null;
        }
        return mst;
    }

    /* A comparator to help you compare vertices in terms of
     * how close they are to the current MST.
     * Feel free to uncomment the below code if you'd like to use it;
     * otherwise, you may implement your own comparator.
     */
    private class PrimVertexComparator implements Comparator<Integer> {
        private HashMap<Integer, Edge> distFromTree;

        public PrimVertexComparator(HashMap<Integer, Edge> distFromTree) {
            this.distFromTree = distFromTree;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            int edgeCompRes = distFromTree.get(o1).compareTo(distFromTree.get(o2));
            if (edgeCompRes == 0) {
                return o1 - o2;
            }
            return edgeCompRes;
        }
    }
}
