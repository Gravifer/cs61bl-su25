import java.util.*;

public class Graph {

    private LinkedList<Edge>[] adjLists;
    private int vertexCount;

    private HashMap<Integer, HashMap<Integer, Integer>> distanceMaps = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Integer>> previousMaps = new HashMap<>();

    /* Initializes a graph with NUMVERTICES vertices and no Edges. */
    public Graph(int numVertices) {
        adjLists = (LinkedList<Edge>[]) new LinkedList[numVertices];
        for (int k = 0; k < numVertices; k++) {
            adjLists[k] = new LinkedList<Edge>();
        }
        vertexCount = numVertices;
    }

    /* Adds a directed Edge (V1, V2) to the graph. That is, adds an edge
       in ONE directions, from v1 to v2. */
    public void addEdge(int v1, int v2) {
        addEdge(v1, v2, 0);
    }

    /* Adds an undirected Edge (V1, V2) to the graph. That is, adds an edge
       in BOTH directions, from v1 to v2 and from v2 to v1. */
    public void addUndirectedEdge(int v1, int v2) {
        addUndirectedEdge(v1, v2, 0);
    }

    /* Adds a directed Edge (V1, V2) to the graph with weight WEIGHT. If the
       Edge already exists, replaces the current Edge with a new Edge with
       weight WEIGHT. */
    public void addEdge(int v1, int v2, int weight) {
        // DONE: YOUR CODE HERE
        if (v1 < 0 || v1 >= vertexCount || v2 < 0 || v2 >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        LinkedList<Edge> edges = adjLists[v1];
        for (Edge edge : edges) {
            if (edge.to == v2) {
                // Edge already exists, replace it
                edges.remove(edge);
                edges.add(new Edge(v1, v2, weight));
                return;
            }
        }
        // Edge does not exist, add a new one
        edges.add(new Edge(v1, v2, weight));
    }

    /* Adds an undirected Edge (V1, V2) to the graph with weight WEIGHT. If the
       Edge already exists, replaces the current Edge with a new Edge with
       weight WEIGHT. */
    public void addUndirectedEdge(int v1, int v2, int weight) {
        // DONE: YOUR CODE HERE
        if (v1 < 0 || v1 >= vertexCount || v2 < 0 || v2 >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        // * using a loop of directed edges to represent an undirected edge is a bad idea, but hey, what can you do
        addEdge(v1, v2, weight);
        addEdge(v2, v1, weight);
    }

    /* Returns true if there exists an Edge from vertex FROM to vertex TO.
       Returns false otherwise. */
    public boolean isAdjacent(int from, int to) {
        // DONE: YOUR CODE HERE
        if (from < 0 || from >= vertexCount || to < 0 || to >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        LinkedList<Edge> edges = adjLists[from];
        for (Edge edge : edges) {
            if (edge.to == to) {
                return true;
            }
        }
        return false;
    }

    /* Returns a list of all the vertices u such that the Edge (V, u)
       exists in the graph. */
    public List<Integer> neighbors(int v) {
        // DONE: YOUR CODE HERE
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        LinkedList<Edge> edges = adjLists[v];
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (Edge edge : edges) {
            neighbors.add(edge.to);
        }
        neighbors.sort((Integer i1, Integer i2) -> -(i1 - i2));
        return neighbors;
    }
    /* Returns the number of incoming Edges for vertex V. */
    public int inDegree(int v) {
        // DONE: YOUR CODE HERE
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        int count = 0;
        for (int i = 0; i < vertexCount; i++) { // ! dumb as f
            LinkedList<Edge> edges = adjLists[i];
            for (Edge edge : edges) {
                if (edge.to == v) {
                    count++;
                }
            }
        }
        return count;
    }

    private void dijkstra(int start) {
        if (start < 0 || start >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        // Initialize distances and previous vertices
        HashMap<Integer, Integer> distanceMap = new HashMap<>();
        HashMap<Integer, Integer> previousMap = new HashMap<>();
        for (int i = 0; i < vertexCount; i++) {
            distanceMap.put(i, Integer.MAX_VALUE);
            previousMap.put(i, -1);
        }
        distanceMap.put(start, 0);

        // Priority queue for Dijkstra's algorithm
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(distanceMap::get));
        pq.add(start);

        while (!pq.isEmpty()) {
            int current = pq.poll();
            int currentDistance = distanceMap.get(current);

            for (Edge edge : adjLists[current]) {
                int neighbor = edge.to;
                int weight = edge.weight;
                if (weight < 0) {
                    throw new IllegalArgumentException("Graph contains negative weight edge");
                }
                int newDistance = currentDistance + weight;

                if (newDistance < distanceMap.get(neighbor)) {
                    distanceMap.put(neighbor, newDistance);
                    previousMap.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        distanceMaps.put(start, distanceMap);
        previousMaps.put(start, previousMap);
    }

    /* Returns a list of the vertices that lie on the shortest path from start to stop.
    If no such path exists, you should return an empty list. If START == STOP, returns a List with START. */
    public ArrayList<Integer> shortestPath(int start, int stop) {
        // TODO: YOUR CODE HERE
        // if the map from start has not been computed yet, run dijkstra's algorithm
        if (!distanceMaps.containsKey(start)) {
            dijkstra(start);
        }
        // if the map from start does not contain stop, there is no path
        if (!distanceMaps.get(start).containsKey(stop)) {
            return new ArrayList<>();
        }
        // reconstruct the path from start to stop
        ArrayList<Integer> path = new ArrayList<>();
        int current = stop;
        if (start == stop) {
            path.add(start);
            return path; // if start == stop, return a list with start
        }
        // traverse the previous maps to find the path
        if (!previousMaps.containsKey(start) || !previousMaps.get(start).containsKey(stop)) {
            return new ArrayList<>(); // no path found
        }
        var previous = previousMaps.get(start);
        while (current != start) {
            path.add(current);
            // find the previous vertex in the path
            current = previous.get(current);
            if (current == -1) {
                return new ArrayList<>(); // no path found
            }
        }
        path.add(start);
        Collections.reverse(path); // reverse the path to get it from start to stop
        return path;
    }

    private Edge getEdge(int v1, int v2) {
        // DONE: YOUR CODE HERE
        if (v1 < 0 || v1 >= vertexCount || v2 < 0 || v2 >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        LinkedList<Edge> edges = adjLists[v1];
        for (Edge edge : edges) {
            if (edge.to == v2) {
                return edge;
            }
        } // Edge does not exist
        return null;
    }

    private class Edge {

        private int from;
        private int to;
        private int weight;

        Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public String toString() {
            return "(" + from + ", " + to + ", weight = " + weight + ")";
        }

        public int to() {
            return this.to;
        }

    }

    
}
