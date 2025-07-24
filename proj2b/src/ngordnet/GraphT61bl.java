package ngordnet;

import com.google.common.collect.Multiset;

import java.util.ArrayList;

/** A <em>directed</em> graph represented as an adjacency list.
 * Each node is represented by a Node object, which contains
 * the vertex index and its payload.
 * Cycles, duplicate edges and self-loops are allowed.
 */
public class GraphT61bl<Tv, Te> {
    private final ArrayList<Node<Tv>> nodes;
    private final Multiset<Edge<Te>> edges;
    public record Node<Tv>(int vertex, Tv get) {}
    public record Edge<Te>(int from, int to, Te get) {}

    /** Constructs a new empty graph. */
    public GraphT61bl() {
        this.nodes = new java.util.ArrayList<>();
        this.edges = com.google.common.collect.HashMultiset.create();
    }

    /** Adds a node with the given payload to the graph.
     * Returns the index of the newly added vertex.
     */
    public int addNode(Tv payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        int index = nodes.size();
        addNode(index, payload);
        return index;
    }

    public int addVertex() {
        int index = nodes.size();
        addVertex(index);
        return index;
    }

    /** Adds a vertex with the given value at the specified vertex.
     * If the vertex is already occupied, it throws an IndexOutOfBoundsException.
     */
    public void addNode(int index, Tv value) {
        if (index < 0 || index > nodes.size()) {
            throw new IndexOutOfBoundsException("Vertex vertex out of bounds");
        }
        if (index == nodes.size()) {
            nodes.add(new Node<>(index, value));
        } else {
            throw new IndexOutOfBoundsException("Index already occupied by another vertex");
        }
    }

    public void addVertex(int index) {
        if (index < 0 || index > nodes.size()) {
            throw new IndexOutOfBoundsException("Vertex vertex out of bounds");
        }
        if (index == nodes.size()) {
            nodes.add(new Node<>(index, null)); // Add a vertex with no payload
        } else {
            throw new IndexOutOfBoundsException("Index already occupied by another vertex");
        }
    }

    /** Returns the vertex at the given vertex.
     * Throws IndexOutOfBoundsException if the vertex is invalid.
     */
    public Node<Tv> getVertex(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Vertex vertex out of bounds");
        }
        return nodes.get(index);
    }

    /** Returns the number of vertices in the graph. */
    public int numVertices() {
        return nodes.size();
    }

    /** Returns the number of edges in the graph. */
    public int numEdges() {
        return edges.size();
    }

    /** Adds a directed edge from vertex f to vertex t with the given value.
     * If the edge already exists, it is not added again.
     * Returns true if the edge was added, false otherwise.
     */
    public boolean addEdge(int f, int t, Te value) {
        if (f < 0 || f >= nodes.size() || t < 0 || t >= nodes.size()) {
            throw new IndexOutOfBoundsException("Vertex vertex out of bounds");
        }
        Edge<Te> edge = new Edge<>(f, t, value);
        return edges.add(edge);
    }

    /** Unit type */
    public static final class Unit {
        public static final Unit INSTANCE = new Unit();
        private Unit() {}
        @Override
        public boolean equals(Object obj) { return obj instanceof Unit; }
        @Override
        public int hashCode() { return 0; }
        @Override
        public String toString() { return "()"; }
    }
}
