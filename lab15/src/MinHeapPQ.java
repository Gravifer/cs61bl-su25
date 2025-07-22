import java.util.NoSuchElementException;

/* A PriorityQueue class that uses a min heap to maintain ordering. */
public class MinHeapPQ<T> implements PriorityQueue<T> {

    /* The heap backing our MinHeapPQ. */
    private final MinHeap<PriorityItem> heap;

    /* Initializes an empty MinHeapPQ. */
    public MinHeapPQ() {
        heap = new MinHeap<PriorityItem>();
    }

    /* Returns the item with the smallest priority value, but does not remove it
     * from the MinHeapPQ. */
    public T peek() {
        // DONE: YOUR CODE HERE
        if (!heap.isEmpty()) {
            return heap.findMin().item();
        }
        return null;
    }

    /* Inserts ITEM with the priority value PRIORITYVALUE into the MinHeapPQ. If
     * ITEM is already in the MinHeapPQ, throw an IllegalArgumentException. */
    public void insert(T item, double priorityValue) {
        // DONE: YOUR CODE HERE
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (contains(item)) {
            throw new IllegalArgumentException("Item already exists in the MinHeapPQ");
        }
        PriorityItem priorityItem = new PriorityItem(item, priorityValue);
        heap.insert(priorityItem);
    }

    /* Returns the item with the highest priority (smallest priority value), and
     * removes it from the MinHeapPQ. If there is nothing in the queue, return null.*/
    public T poll() {
        // DONE: YOUR CODE HERE
        if (!heap.isEmpty()) {
            PriorityItem minItem = heap.removeMin();
            return minItem.item();
        }
        return null;
    }

    /* Changes the PriorityItem with item ITEM to have priority value
     * PRIORITYVALUE. Assume the items in the MinHeapPQ are all unique. If ITEM
     * is not in the MinHeapPQ, throw a NoSuchElementException. */
    public void changePriority(T item, double priorityValue) {
        // DONE: OPTIONAL
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (!contains(item)) {
            throw new NoSuchElementException("Item not found in MinHeapPQ");
        }
        // // Find the existing PriorityItem
        // PriorityItem existingItem = new PriorityItem(item, 0);
        // int index = heap.indexOf(existingItem);
        // if (index == -1) {
        //     throw new NoSuchElementException("Item not found in MinHeapPQ");
        // }
        // // ! abstraction forbids modifying the priority value in place

        PriorityItem updatedItem = new PriorityItem(item, priorityValue);
        heap.update(updatedItem);
    }

    /* Returns the number of items in the MinHeapPQ. */
    public int size() {
        // DONE: YOUR CODE HERE
        if (heap == null) {
            throw new IllegalStateException("Heap is not initialized");
        }
        return heap.size();
    }

    /* Returns true if ITEM is stored in our MinHeapPQ. Note: Any priority value
     * for this dummy PriorityItem would work. */
    public boolean contains(T item) {
        return heap.contains(new PriorityItem(item, 0));
    }

    @Override
    public String toString() {
        return heap.toString();
    }

    /* A wrapper class that stores items and their associated priorities.

     * Note: This class has a natural ordering that is inconsistent with
     * equals. */
    public class PriorityItem implements Comparable<PriorityItem> {
        private final T item;
        private double priorityValue;

        private PriorityItem(T item, double priorityValue) {
            this.item = item;
            this.priorityValue = priorityValue;
        }

        public T item() {
            return this.item;
        }

        public double priorityValue() {
            return this.priorityValue;
        }

        @Override
        public String toString() {
            return "(PriorityItem: " + this.item.toString() + ", "
                    + this.priorityValue + ")";
        }

        @Override
        public int compareTo(PriorityItem o) { // ! compared only by priorityValue
            double diff = this.priorityValue - o.priorityValue;
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object o) { // ! compared only by item
            if (o == null) {
                return false;
            } else if (getClass() == o.getClass()) {
                PriorityItem p = (PriorityItem) o;
                return p.item.equals(item);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }
    }
}
