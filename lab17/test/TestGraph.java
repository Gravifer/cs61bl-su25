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

        assertWithMessage("Expected size 0 path between 2 vertices, no edges.").that(path1.isEmpty()).isTrue();
        assertWithMessage("Expected size 0 path between 2 vertices, no edges.").that(path2.isEmpty()).isTrue();
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

    @Test
    public void testPathExistsBasic() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertThat(g.pathExists(0, 2)).isTrue();
        assertThat(g.pathExists(0, 1)).isTrue();
        assertThat(g.pathExists(1, 0)).isFalse();
        assertThat(g.pathExists(2, 0)).isFalse();
        assertThat(g.pathExists(2, 2)).isTrue(); // self path
    }

    @Test
    public void testPathExistsDisconnected() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(2, 3, 1);
        assertThat(g.pathExists(0, 3)).isFalse();
        assertThat(g.pathExists(2, 1)).isFalse();
        assertThat(g.pathExists(2, 3)).isTrue();
    }

    @Test
    public void testPathBasic() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        List<Integer> path = g.path(0, 2);
        assertThat(path).containsExactly(0, 1, 2).inOrder();
        assertThat(g.path(2, 0)).isEmpty();
        assertThat(g.path(0, 0)).containsExactly(0);
    }

    @Test
    public void testPathDisconnected() {
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(2, 3, 1);
        assertThat(g.path(0, 3)).isEmpty();
        assertThat(g.path(2, 1)).isEmpty();
        assertThat(g.path(2, 3)).containsExactly(2, 3).inOrder();
    }

    @Test
    public void testPathCycle() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        List<Integer> path = g.path(0, 2);
        assertThat(path.getFirst()).isEqualTo(0);
        assertThat(path.getLast()).isEqualTo(2);
        assertThat(g.path(2, 1)).contains(2);
    }

    @Test
    public void testPathWithGenerateG1() {
        Graph g = new Graph(5);
        g.generateG1();
        // Path from 0 to 3: 0->2->3 or 0->4->3
        List<Integer> path1 = g.path(0, 3);
        assertThat(path1.getFirst()).isEqualTo(0);
        assertThat(path1.getLast()).isEqualTo(3);
        assertThat(g.pathExists(0, 3)).isTrue();
        // Path from 4 to 0: no path
        assertThat(g.path(4, 0)).isEmpty();
        assertThat(g.pathExists(4, 0)).isFalse();
    }

    @Test
    public void testPathWithGenerateG2() {
        Graph g = new Graph(5);
        g.generateG2();
        // Path from 0 to 3: 0->2->3 or 0->4->3
        List<Integer> path1 = g.path(0, 3);
        assertThat(path1.getFirst()).isEqualTo(0);
        assertThat(path1.getLast()).isEqualTo(3);
        assertThat(g.pathExists(0, 3)).isTrue();
        // Path from 3 to 0: no path
        assertThat(g.path(3, 0)).isEmpty();
        assertThat(g.pathExists(3, 0)).isFalse();
    }

    @Test
    public void testPathWithGenerateG3() {
        Graph g = new Graph(7);
        g.generateG3();
        assertThat(g.pathExists(0, 6)).isTrue();
        List<Integer> path = g.path(0, 6);
        assertThat(path.getFirst()).isEqualTo(0);
        assertThat(path.getLast()).isEqualTo(6);
        // Path from 0 to 3
        List<Integer> path2 = g.path(0, 3);
        assertThat(path2.getFirst()).isEqualTo(0);
        assertThat(path2.getLast()).isEqualTo(3);
    }

    @Test
    public void testPathWithGenerateG4() {
        Graph g = new Graph(5);
        g.generateG4();
        // There is a cycle: 0->1->2->0
        assertThat(g.pathExists(0, 2)).isTrue();
        List<Integer> path = g.path(0, 2);
        assertThat(path.getFirst()).isEqualTo(0);
        assertThat(path.getLast()).isEqualTo(2);
        // 4->2 exists
        assertThat(g.pathExists(4, 2)).isTrue();
        // 3->0 does not exist
        assertThat(g.pathExists(3, 0)).isFalse();
        assertThat(g.path(3, 0)).isEmpty();
    }

    @Test
    public void testTopologicalSortSimpleChain() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        List<Integer> topo = g.topologicalSort();
        assertThat(topo).containsExactly(0, 1, 2).inOrder();
    }

    @Test
    public void testTopologicalSortMultipleStarts() {
        Graph g = new Graph(4);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        List<Integer> topo = g.topologicalSort();
        System.out.println(topo);
        // 0 and 1 can be in any order before 2, then 3
        assertThat(topo.indexOf(2)).isGreaterThan(topo.indexOf(0));
        assertThat(topo.indexOf(2)).isGreaterThan(topo.indexOf(1));
        assertThat(topo.indexOf(3)).isGreaterThan(topo.indexOf(2));
    }

    @Test
    public void testTopologicalSortDisconnected() {
        Graph g = new Graph(5);
        g.addEdge(0, 1);
        g.addEdge(2, 3);
        List<Integer> topo = g.topologicalSort();
        // 0 before 1, 2 before 3, 4 can be anywhere
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(1));
        assertThat(topo.indexOf(2)).isLessThan(topo.indexOf(3));
        assertThat(topo).contains(4);
    }

    @Test
    public void testTopologicalSortSingleNode() {
        Graph g = new Graph(1);
        List<Integer> topo = g.topologicalSort();
        assertThat(topo).containsExactly(0);
    }

    @Test
    public void testTopologicalSortEmptyGraph() {
        Graph g = new Graph(0);
        List<Integer> topo = g.topologicalSort();
        assertThat(topo).isEmpty();
    }

    @Test
    public void testTopologicalSortWithCycle() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0); // cycle
        // Should not throw, but will not return a valid topological order
        List<Integer> topo = g.topologicalSort();
        // In a cycle, not all nodes can be sorted, so result may be incomplete
        assertThat(topo.size()).isLessThan(3);
    }

    @Test
    public void testTopologicalSortWithGenerateG1() {
        Graph g = new Graph(5);
        g.generateG1();
        List<Integer> topo = g.topologicalSort();
        // 0 must come before 1,2,4; 1 before 2; 2 before 3; 4 before 3
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(1));
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(2));
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(4));
        assertThat(topo.indexOf(1)).isLessThan(topo.indexOf(2));
        assertThat(topo.indexOf(2)).isLessThan(topo.indexOf(3));
        assertThat(topo.indexOf(4)).isLessThan(topo.indexOf(3));
    }

    @Test
    public void testTopologicalSortWithGenerateG2() {
        Graph g = new Graph(5);
        g.generateG2();
        List<Integer> topo = g.topologicalSort();
        // 0 must come before 1,2,4; 1 before 2; 2 before 3; 4 before 3
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(1));
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(2));
        assertThat(topo.indexOf(0)).isLessThan(topo.indexOf(4));
        assertThat(topo.indexOf(1)).isLessThan(topo.indexOf(2));
        assertThat(topo.indexOf(2)).isLessThan(topo.indexOf(3));
        assertThat(topo.indexOf(4)).isLessThan(topo.indexOf(3));
    }

    @Test
    public void testTopologicalSortWithMultipleZeroInDegree() {
        Graph g = new Graph(4);
        // 0->2, 1->2, 2->3
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        List<Integer> topo = g.topologicalSort();
        // 0 and 1 can be in any order before 2, then 3
        assertThat(topo.indexOf(2)).isGreaterThan(topo.indexOf(0));
        assertThat(topo.indexOf(2)).isGreaterThan(topo.indexOf(1));
        assertThat(topo.indexOf(3)).isGreaterThan(topo.indexOf(2));
    }

}
