import org.junit.Test;

import static com.google.common.truth.Truth.*;

public class MinHeapTest {
    @Test
    public void testAddOneThing() {
        MinHeap<String> heap = new MinHeap<>();
        heap.insert("l");
        String item = heap.findMin();
        assertThat(item).isEqualTo("l");
    }

    @Test
    public void testAddThenRemove() {
        MinHeap<String> heap = new MinHeap<>();
        heap.insert("h");
        heap.insert("i");
        String item = heap.removeMin();
        assertThat(item).isEqualTo("h");
        assertThat(heap.findMin()).isEqualTo("i");
    }

    /**
     * Tests that a MinHeap can add and remove a single element.
     */
    @Test
    public void testOneThing() {
        MinHeap<String> heap = new MinHeap<>();
        assertThat(heap.removeMin()).isNull();
        heap.insert("l");
        assertThat(heap.size()).isEqualTo(1);
        String item = heap.removeMin();
        assertThat(item).isEqualTo("l");
        assertThat(heap.size()).isEqualTo(0);
    }

    @Test
    public void testMultipleInsertAndOrder() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.insert(5);
        heap.insert(3);
        heap.insert(8);
        heap.insert(1);
        heap.insert(4);
        assertThat(heap.findMin()).isEqualTo(1);
        assertThat(heap.size()).isEqualTo(5);
        assertThat(heap.removeMin()).isEqualTo(1);
        assertThat(heap.removeMin()).isEqualTo(3);
        assertThat(heap.removeMin()).isEqualTo(4);
        assertThat(heap.removeMin()).isEqualTo(5);
        assertThat(heap.removeMin()).isEqualTo(8);
        assertThat(heap.removeMin()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertDuplicateThrows() {
        MinHeap<String> heap = new MinHeap<>();
        heap.insert("a");
        heap.insert("a"); // 应抛出异常
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertNullThrows() {
        MinHeap<String> heap = new MinHeap<>();
        heap.insert(null);
    }

    @Test
    public void testContains() {
        MinHeap<String> heap = new MinHeap<>();
        heap.insert("x");
        heap.insert("y");
        assertThat(heap.contains("x")).isTrue();
        assertThat(heap.contains("y")).isTrue();
        assertThat(heap.contains("z")).isFalse();
    }

    @Test
    public void testEmptyHeapFindMinRemoveMin() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertThat(heap.findMin()).isNull();
        assertThat(heap.removeMin()).isNull();
    }

    @Test
    public void testSizeAfterInsertAndRemove() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertThat(heap.size()).isEqualTo(0);
        heap.insert(10);
        heap.insert(20);
        assertThat(heap.size()).isEqualTo(2);
        heap.removeMin();
        assertThat(heap.size()).isEqualTo(1);
        heap.removeMin();
        assertThat(heap.size()).isEqualTo(0);
    }

}
