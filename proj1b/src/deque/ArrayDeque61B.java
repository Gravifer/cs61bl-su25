package deque;
import java.util.Iterator;
import java.util.List;
import java.lang.Math;
import java.util.NoSuchElementException;

public class ArrayDeque61B<T> implements Deque61B<T> {
    // This class implements a deque using an array.
    private T[] items; // Initial capacity of 8 per spec
    private int head = 0;
    private int size; // Tracks the number of elements in the deque

    public ArrayDeque61B() {
        this(8); // Initial capacity of 8 per spec
    }

    @SuppressWarnings("unchecked")
    public ArrayDeque61B(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than 0");
        }
        items = (T[]) new Object[initialCapacity]; // Initialize with specified capacity
        size = 0; // Start with size 0
    }

    @SuppressWarnings({"unchecked", "ManualArrayCopy"})
    private void grow() {
        // grow the array to double its current size
        T[] newItems = (T[]) new Object[items.length * 2];
        // Copy existing elements to the new array
        for (int i = 0; i < size; i++) {
            newItems[i] = items[head + i];
        }
        head = 0; // Reset head to 0 since we are copying elements from the start
        items = newItems; // Update the reference to the new array
    }

    @SuppressWarnings({"unchecked", "ManualArrayCopy"})
    private void shrink() {
        // shrink the array to half its current size
        T[] newItems = (T[]) new Object[items.length / 2];
        // Copy existing elements to the new array
        for (int i = 0; i < size; i++) {
            newItems[i] = items[head + i];
        }
        head = 0; // Reset head to 0 since we are copying elements from the start
        items = newItems; // Update the reference to the new array
    }

    /**
     * This method is used to inspect the size of the backing array for testing purposes.
     * It is not part of the public API and should not be used outside of tests.
     */
    protected int getBackingArrayLength() {
        return items.length; // Access the size of the backing array
    }

    /**
     * Add {@code x} to the front of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    @Override
    public void addFirst(T x) {
        if (x == null) {
            throw new IllegalArgumentException("Cannot add null to the deque");
        }
        // Ensure there's enough space, otherwise grow
        if (size == items.length) {
            grow();
        }
        // // Shift elements to the right to make space at the front
        // for (int i = size; i > 0; i--) {
        //     items[i] = items[i - 1];
        // }
        // items[0] = x; // Add the new item at the front

        // instead of shifting, offset based on the index
        int newIndex = Math.floorMod(head - 1, items.length); // Calculate new index for the front
        items[newIndex] = x; // Place the new item at the front
        head = newIndex; // Update the head to the new front
        size++;
    }

    /**
     * Add {@code x} to the back of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    @Override
    public void addLast(T x) {
        if (x == null) {
            throw new IllegalArgumentException("Cannot add null to the deque");
        }
        // Ensure there's enough space, otherwise grow
        if (size == items.length) {
            grow();
        }
        items[Math.floorMod(head + size, items.length)] = x; // Place the new item at the back
        size++;
    }

    /**
     * Returns a List copy of the deque. Does not alter the deque.
     *
     * @return a new list copy of the deque.
     */
    @Override
    public List<T> toList() {
        if (size == 0) {
            return List.of(); // Return an empty list if the deque is empty
        }

        // Create a new list and copy elements from the deque
        List<T> list = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(items[Math.floorMod(head + i, items.length)]);
        }
        return list; // Return the new list
    }

    /**
     * Returns if the deque is empty. Does not alter the deque.
     *
     * @return {@code true} if the deque has no elements, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return size == 0; // Check if size is 0 to determine if the deque is empty
    }

    /**
     * Returns the size of the deque. Does not alter the deque.
     *
     * @return the number of items in the deque.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Remove and return the element at the front of the deque if it exists.
     *
     * @return removed element, otherwise {@code null}.
     */
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null; // Return null if the deque is empty
        }
        T removedItem = items[head]; // Get the item at the front
        items[head] = null; // Clear the item at the front
        head = Math.floorMod(head + 1, items.length); // Move head to the next position
        size--; // Decrease size
        // If the size is less than a quarter of the array length, shrink the array
        if (size < items.length / 4 && items.length > 8) {
            shrink(); // Shrink the array if necessary
        }
        return removedItem; // Return the removed item
    }

    /**
     * Remove and return the element at the back of the deque if it exists.
     *
     * @return removed element, otherwise {@code null}.
     */
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null; // Return null if the deque is empty
        }
        int lastIndex = Math.floorMod(head + size - 1, items.length); // Calculate the index of the last item
        T removedItem = items[lastIndex]; // Get the item at the back
        items[lastIndex] = null; // Clear the item at the back
        size--; // Decrease size
        // If the size is less than a quarter of the array length, shrink the array
        if (size < items.length / 4 && items.length > 8) {
            shrink(); // Shrink the array if necessary
        }
        return removedItem; // Return the removed item
    }

    /**
     * The Deque61B abstract data type does not typically have a get method,
     * but we've included this extra operation to provide you with some
     * extra programming practice. Gets the element at the given index. Returns
     * null if index is out of bounds. Does not alter the deque.
     *
     * @param index index to get
     * @return element at {@code index} in the deque
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null; // Return null if index is out of bounds
        }
        int actualIndex = Math.floorMod(this.head + index, items.length); // Calculate the actual index in the array
        return items[actualIndex]; // Return the item at the calculated index
    }

    /**
     * This method technically shouldn't be in the interface, but it's here
     * to make testing nice. Gets an element, recursively. Returns null if
     * index is out of bounds. Does not alter the deque.
     *
     * @param index index to get
     * @return element at {@code index} in the deque
     */
    @Override
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null; // Return null if index is out of bounds
        }
        // Base case: if index is 0, return the item at the current head
        if (index == 0) {
            return items[Math.floorMod(this.head, items.length)];
        }
        // Recursive case: get the item at the next index
        return getRecursive(index - 1);
    }

    @Override
    public String toString() {
        // if (isEmpty()) {
        //     return "[]"; // Return empty representation if deque is empty
        // }
        // StringBuilder sb = new StringBuilder("[");
        // for (int i = 0; i < size; i++) {
        //     sb.append(get(i)); // Append each item in the deque
        //     if (i < size - 1) {
        //         sb.append(", "); // Add a comma and space between items
        //     }
        // }
        // sb.append("]"); // Close the string representation
        // return sb.toString(); // Return the string representation of the deque
        return Deque61BToString(); // Use the default implementation from the interface
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if both references point to the same object
        if (!(o instanceof Deque61B<?> other)) return false; // Check if the other object is an instance of ArrayDeque61B
        // if (this.size != other.size) return false; // Check if sizes are equal
        //
        // // Compare elements in the deque
        // for (int i = 0; i < size; i++) {
        //     if (!get(i).equals(other.get(i))) {
        //         return false; // Return false if any element is not equal
        //     }
        // }
        // return true; // All elements are equal, return true
        return equalsDeque61B(other);
    }

    @Override
    public int hashCode() {
        int result = 1; // Initialize hash code
        for (int i = 0; i < size; i++) {
            T item = get(i); // Get each item in the deque
            result = 31 * result + (item == null ? 0 : item.hashCode()); // Update hash code with item's hash code
        }
        return result; // Return the final computed hash code
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<T> iterator() {
        return new ArrayDeque61BIterator();
    }

    private class ArrayDeque61BIterator implements Iterator<T> {
        private int index;

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return index < size; // Check if there are more elements to iterate
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the ArrayDeque61B iterator.");
            }
            T item = items[Math.floorMod(head + index, items.length)]; // Get the item at the current index
            index++; // Move to the next index
            return item; // Return the item
        }
    }
}
