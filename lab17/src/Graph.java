import edu.princeton.cs.algs4.Edge;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Stack;
import java.util.HashSet;

public class Graph implements Iterable<Integer> {

    private LinkedList<Edge>[] adjLists;
    private int vertexCount;

    /* Initializes a graph with NUMVERTICES vertices and no Edges. */
    public Graph(int numVertices) {
        adjLists = (LinkedList<Edge>[]) new LinkedList[numVertices];
        for (int k = 0; k < numVertices; k++) {
            adjLists[k] = new LinkedList<Edge>();
        }
        vertexCount = numVertices;
    }

    /* Adds a directed Edge (V1, V2) to the graph. That is, adds an edge
     * in ONE directions, from v1 to v2. */
    public void addEdge(int v1, int v2) {
        addEdge(v1, v2, 0);
    }

    /* Adds a directed Edge (V1, V2) to the graph with weight WEIGHT. If the
     * Edge already exists, replaces the current Edge with a new Edge with
     * weight WEIGHT. */
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

    /* Adds an undirected Edge (V1, V2) to the graph. That is, adds an edge
     * in BOTH directions, from v1 to v2 and from v2 to v1. */
    public void addUndirectedEdge(int v1, int v2) {
        addUndirectedEdge(v1, v2, 0);
    }

    /* Adds an undirected Edge (V1, V2) to the graph with weight WEIGHT. If the
     * Edge already exists, replaces the current Edge with a new Edge with
     * weight WEIGHT. */
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
     * Returns false otherwise. */
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
     * exists in the graph. */
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

    /* Returns an Iterator that outputs the vertices of the graph in topological
     * sorted order. */
    public Iterator<Integer> iterator() {
        return new TopologicalIterator();
    }

    /**
     *  A class that iterates through the vertices of this graph,
     *  starting with a given vertex. Does not necessarily iterate
     *  through all vertices in the graph: if the iteration starts
     *  at a vertex v, and there is no path from v to a vertex w,
     *  then the iteration will not include w.
     */
    private class DFSIterator implements Iterator<Integer> {

        private Stack<Integer> fringe;
        private HashSet<Integer> visited;

        public DFSIterator(Integer start) {
            fringe = new Stack<>();
            visited = new HashSet<>();
            fringe.push(start);
        }

        public boolean hasNext() {
            if (!fringe.isEmpty()) {
                int i = fringe.pop();
                while (visited.contains(i)) {
                    if (fringe.isEmpty()) {
                        return false;
                    }
                    i = fringe.pop();
                }
                fringe.push(i);
                return true;
            }
            return false;
        }

        public Integer next() {
            int curr = fringe.pop();
            ArrayList<Integer> lst = new ArrayList<>();
            for (int i : neighbors(curr)) {
                lst.add(i);
            }
            lst.sort((Integer i1, Integer i2) -> -(i1 - i2));
            for (Integer e : lst) {
                fringe.push(e);
            }
            visited.add(curr);
            return curr;
        }

        //ignore this method
        public void remove() {
            throw new UnsupportedOperationException(
                    "vertex removal not implemented");
        }

    }

    /* Returns the collected result of performing a depth-first search on this
     * graph's vertices starting from V. */
    public List<Integer> dfs(int v) {
        ArrayList<Integer> result = new ArrayList<Integer>(); // * just repeats what's in iter.visited except ordered
        Iterator<Integer> iter = new DFSIterator(v);

        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    /* Returns true iff there exists a path from START to STOP. Assumes both
     * START and STOP are in this graph. If START == STOP, returns true. */
    public boolean pathExists(int start, int stop) {
        // DONE: YOUR CODE HERE
        if (start < 0 || start >= vertexCount || stop < 0 || stop >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        if (start == stop) {
            return true;
        }
        // Stack<Integer> stack = new Stack<>();
        // HashSet<Integer> visited = new HashSet<>();
        // stack.push(start);
        // while (!stack.isEmpty()) {
        //     int current = stack.pop();
        //     if (current == stop) {
        //         return true;
        //     }
        //     if (!visited.contains(current)) {
        //         visited.add(current);
        //         for (int neighbor : neighbors(current)) {
        //             if (!visited.contains(neighbor)) {
        //                 stack.push(neighbor);
        //             }
        //         }
        //     }
        // }
        // use DFSIterator to find the path
        if (dfs(start).contains(stop)) {
            return true;
        }
        return false;
    }


    /* Returns the path from START to STOP. If no path exists, returns an empty
     * List. If START == STOP, returns a List with START. */
    public List<Integer> path(int start, int stop) {
        // DONE: YOUR CODE HERE
        if (start < 0 || start >= vertexCount || stop < 0 || stop >= vertexCount) {
            throw new IllegalArgumentException("Vertex out of bounds");
        }
        ArrayList<Integer> path = new ArrayList<>();
        if (start == stop) {
            path.add(start);
            return path;
        }
        if (!pathExists(start, stop)) {
            return path; // return empty list if no path exists
        }
        DFSIterator iter = new DFSIterator(start);
        // // need to keep track of whether a path attempt is discarded
        // // you know this happened when stop is not reached but the size of the fringe is reduced
        // int fringeSize = iter.fringe.size();
        // boolean pathFound = false;
        // while (iter.hasNext()) {
        //     int current = iter.next();
        //     path.add(current);
        //     if (current == stop) {
        //         pathFound = true;
        //         break;
        //     }
        //     // if the size of the fringe is reduced, it means we have to backtrack
        //     if (iter.fringe.size() < fringeSize) {
        //         fringeSize = iter.fringe.size();
        //         path.remove(path.size() - 1); // remove the last element added to the path
        //     }
        // }
        // if (!pathFound) {
        //     path.clear(); // clear the path if no path was found
        // } else {
        //     path.add(stop); // add the stop vertex to the path
        // }
        // * forget it. We just do backtracking using neighbors()
        while (iter.hasNext()) {
            int next = iter.next();
            // * if next cannot be reached from the last element of path, it means backtracking happened
            if (!path.isEmpty() && !isAdjacent(path.getLast(), next)) {
                path.removeLast(); // clear the path if backtracking happened
            }
            path.add(next);
            if (next == stop) {
                return path; // return the path if stop is reached            }
            }
        }
        path.clear();
        return path;
    }

    public List<Integer> topologicalSort() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        Iterator<Integer> iter = new TopologicalIterator();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    private class TopologicalIterator implements Iterator<Integer> {

        private Stack<Integer> fringe;

        private int[] currentInDegree;

        TopologicalIterator() {
            fringe = new Stack<Integer>();
            // TODO: YOUR CODE HERE
        }

        public boolean hasNext() {
            // TODO: YOUR CODE HERE
            return false;
        }

        public Integer next() {
            // TODO: YOUR CODE HERE
            return 0;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

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

    }

    private void generateG1() {
        addEdge(0, 1);
        addEdge(0, 2);
        addEdge(0, 4);
        addEdge(1, 2);
        addEdge(2, 0);
        addEdge(2, 3);
        addEdge(4, 3);
    }

    private void generateG2() {
        addEdge(0, 1);
        addEdge(0, 2);
        addEdge(0, 4);
        addEdge(1, 2);
        addEdge(2, 3);
        addEdge(4, 3);
    }

    private void generateG3() {
        addUndirectedEdge(0, 2);
        addUndirectedEdge(0, 3);
        addUndirectedEdge(1, 4);
        addUndirectedEdge(1, 5);
        addUndirectedEdge(2, 3);
        addUndirectedEdge(2, 6);
        addUndirectedEdge(4, 5);
    }

    private void generateG4() {
        addEdge(0, 1);
        addEdge(1, 2);
        addEdge(2, 0);
        addEdge(2, 3);
        addEdge(4, 2);
    }

    private void printDFS(int start) {
        System.out.println("DFS traversal starting at " + start);
        List<Integer> result = dfs(start);
        Iterator<Integer> iter = result.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next() + " ");
        }
        System.out.println();
        System.out.println();
    }

    private void printPath(int start, int end) {
        System.out.println("Path from " + start + " to " + end);
        List<Integer> result = path(start, end);
        if (result.size() == 0) {
            System.out.println("No path from " + start + " to " + end);
            return;
        }
        Iterator<Integer> iter = result.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next() + " ");
        }
        System.out.println();
        System.out.println();
    }

    private void printTopologicalSort() {
        System.out.println("Topological sort");
        List<Integer> result = topologicalSort();
        Iterator<Integer> iter = result.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next() + " ");
        }
    }

    public static void main(String[] args) {
        Graph g1 = new Graph(5);
        g1.generateG1();
        g1.printDFS(0);
        g1.printDFS(2);
        g1.printDFS(3);
        g1.printDFS(4);

        g1.printPath(0, 3);
        g1.printPath(0, 4);
        g1.printPath(1, 3);
        g1.printPath(1, 4);
        g1.printPath(4, 0);

        Graph g2 = new Graph(5);
        g2.generateG2();
        g2.printTopologicalSort();
    }
}
