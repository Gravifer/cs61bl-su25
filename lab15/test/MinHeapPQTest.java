import org.junit.Test;

import static com.google.common.truth.Truth.*;


public class MinHeapPQTest {

    @Test
    public void testAddOneThing() {
        MinHeapPQ<String> pq = new MinHeapPQ<>();
        pq.insert("l", 2);
        String item = pq.poll();
        assertThat("l").isEqualTo(item);
    }

    @Test
    public void testAddThenRemove() {
        MinHeapPQ<String> pq = new MinHeapPQ<>();
        pq.insert("h", 100);
        pq.insert("i", 0);
        String item = pq.poll();
        assertThat("i").isEqualTo(item);
        assertThat("h").isEqualTo(pq.poll());
    }

    /**
     * Tests that a MinHeapPQ can add and remove a single element.
     */
    @Test
    public void testOneThing() {
        MinHeapPQ<String> pq = new MinHeapPQ<>();
        assertThat(pq.poll()).isNull();
        pq.insert("l", 2);
        assertThat(1).isEqualTo(pq.size());
        String item = pq.poll();
        assertThat("l").isEqualTo(item);
        assertThat(0).isEqualTo(pq.size());
    }

    // DONE: add some of your own tests here!

    @Test
    public void testComplexSequence1() {
        MinHeapPQ<String> pq = new MinHeapPQ<>();
        pq.insert("a", 5);
        pq.insert("b", 2);
        pq.insert("c", 8);
        pq.insert("d", 1);
        assertThat(pq.peek()).isEqualTo("d");
        assertThat(pq.size()).isEqualTo(4);
        pq.changePriority("a", 0.5);
        assertThat(pq.peek()).isEqualTo("a");
        assertThat(pq.poll()).isEqualTo("a");
        assertThat(pq.contains("a")).isFalse();
        assertThat(pq.poll()).isEqualTo("d");
        pq.insert("e", 0.1);
        assertThat(pq.peek()).isEqualTo("e");
        pq.changePriority("e", 10);
        assertThat(pq.peek()).isEqualTo("b");
        assertThat(pq.poll()).isEqualTo("b");
        assertThat(pq.poll()).isEqualTo("c");
        assertThat(pq.poll()).isEqualTo("e");
        assertThat(pq.poll()).isNull();
        assertThat(pq.size()).isEqualTo(0);
    }

    @Test
    public void testComplexSequence2() {
        MinHeapPQ<Integer> pq = new MinHeapPQ<>();
        pq.insert(1, 100);
        pq.insert(2, 50);
        pq.insert(3, 75);
        pq.insert(4, 25);
        pq.insert(5, 60);
        assertThat(pq.peek()).isEqualTo(4);
        pq.changePriority(1, 10);
        assertThat(pq.peek()).isEqualTo(1);
        assertThat(pq.poll()).isEqualTo(1);
        pq.changePriority(2, 200);
        assertThat(pq.poll()).isEqualTo(4);
        assertThat(pq.poll()).isEqualTo(5);
        assertThat(pq.contains(2)).isTrue();
        assertThat(pq.poll()).isEqualTo(3);
        assertThat(pq.poll()).isEqualTo(2);
        assertThat(pq.poll()).isNull();
        assertThat(pq.size()).isEqualTo(0);
    }
}
