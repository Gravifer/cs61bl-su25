import org.junit.Test;

import static com.google.common.truth.Truth.*;

public class MinHeapTest {
    // @Test
    // public void testGetLeftOf() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("a");
    //     heap.add("b");
    //     heap.add("c");
    //     assertThat(heap.getLeftOf(1)).isEqualTo(2);
    //     assertThat(heap.getLeftOf(2)).isEqualTo(4);
    //     assertThat(heap.getLeftOf(3)).isEqualTo(6);
    // }
    // @Test
    // public void testGetRightOf() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("a");
    //     heap.add("b");
    //     heap.add("c");
    //     assertThat(heap.getRightOf(1)).isEqualTo(3);
    //     assertThat(heap.getRightOf(2)).isEqualTo(5);
    //     assertThat(heap.getRightOf(3)).isEqualTo(7);
    // }
    // @Test
    // public void testGetParentOf() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("a");
    //     heap.add("b");
    //     heap.add("c");
    //     assertThat(heap.getParentOf(2)).isEqualTo(1);
    //     assertThat(heap.getParentOf(3)).isEqualTo(1);
    //     assertThat(heap.getParentOf(4)).isEqualTo(2);
    // }
    // @Test
    // public void testMin() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("c");
    //     heap.add("a");
    //     heap.add("b");
    //     assertThat(heap.min()).isEqualTo("a");
    // }

    // @Test
    // public void testAddOneThing() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("l");
    //     String item = heap.poll();
    //     assertThat("l").isEqualTo(item);
    // }
    //
    // @Test
    // public void testAddThenRemove() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     heap.add("h");
    //     heap.add("i");
    //     String item = heap.poll();
    //     assertThat("h").isEqualTo(item);
    //     assertThat("i").isEqualTo(heap.poll());
    // }
    //
    // /**
    //  * Tests that a MinHeap can add and remove a single element.
    //  */
    // @Test
    // public void testOneThing() {
    //     MinHeap<String> heap = new MinHeap<>();
    //     assertThat(heap.poll()).isNull();
    //     heap.add("l");
    //     assertThat(1).isEqualTo(heap.size());
    //     String item = heap.poll();
    //     assertThat("l").isEqualTo(item);
    //     assertThat(0).isEqualTo(heap.size());
    // }
}
