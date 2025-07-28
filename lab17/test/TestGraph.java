import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.*;

public class TestGraph {

    @Test
    public void testPath1() {
        Graph g = new Graph(2);
        assertWithMessage("Expected pathExists()==false between 2 vertices, no edges.").that(g.pathExists(0, 1)).isFalse();
        assertWithMessage("Expected pathExists()==false between 2 vertices, no edges.").that(g.pathExists(1, 0)).isFalse();

        List<Integer> path1 = g.path(0, 1);
        List<Integer> path2 = g.path(1, 0);

        assertWithMessage("Expected size 0 path between 2 vertices, no edges.").that(path1.size() == 0);
        assertWithMessage("Expected size 0 path between 2 vertices, no edges.").that(path2.size() == 0);
    }

    // DONE: add more tests!

    @Test
    public void testAddEdgeAndIsAdjacent() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 7);
        assertThat(g.isAdjacent(0, 1)).isTrue();
        assertThat(g.isAdjacent(1, 2)).isTrue();
        assertThat(g.isAdjacent(1, 0)).isFalse();
        assertThat(g.isAdjacent(2, 1)).isFalse();
    }

    @Test
    public void testAddUndirectedEdgeAndIsAdjacent() {
        Graph g = new Graph(3);
        g.addUndirectedEdge(0, 2, 8);
        assertThat(g.isAdjacent(0, 2)).isTrue();
        assertThat(g.isAdjacent(2, 0)).isTrue();
        assertThat(g.isAdjacent(1, 2)).isFalse();
    }

    @Test
    public void testNeighbors() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 2);
        g.addEdge(0, 3, 3);
        List<Integer> neighbors = g.neighbors(0);
        assertThat(neighbors).containsExactly(3, 2, 1).inOrder(); // 按降序
        assertThat(g.neighbors(1)).isEmpty();
    }

    @Test
    public void testInDegree() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(3, 1, 3);
        g.addEdge(1, 2, 4);
        assertThat(g.inDegree(1)).isEqualTo(3);
        assertThat(g.inDegree(2)).isEqualTo(1);
        assertThat(g.inDegree(0)).isEqualTo(0);
    }

}
