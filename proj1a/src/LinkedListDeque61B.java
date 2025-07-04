import java.util.ArrayList;
import java.util.List;
// // import java.util.Optional; // * don't use Optional when you already use a sentinel

public class LinkedListDeque61B<T> implements  Deque61B<T> {
    // This class is a placeholder for the actual implementation of LinkedListDeque61B.
    // You would implement the methods defined in the Deque61B interface here.

    // Example fields and methods could include:
    // private Node<T> front;
    // private Node<T> back;
    // private int size;

    private final Node sentinel; // A sentinel node to simplify the implementation.
    private int size = 0;

    // // public T removeFirst() { ... }
    // // public T removeLast() { ... }
    // // public T get(int index) { ... }
    // // public T getRecursive(int index) { ... }

    public LinkedListDeque61B() {
        // Initialize the sentinel node.
        sentinel = new Node(null);
        sentinel.next = sentinel; // Points to itself, making it circular.
        sentinel.prev = sentinel; // Points to itself, making it circular.
    }

    /**
     * Add {@code x} to the front of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    @Override
    public void addFirst(T x) {
        if (x == null) {
            throw new IllegalArgumentException("Cannot add null to the deque.");
        }
        Node curr = new Node(x, sentinel.next, sentinel);
        sentinel.next.prev = curr;
        sentinel.next = curr;
        size += 1;
    }

    /**
     * Add {@code x} to the back of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    @Override
    public void addLast(T x) {
        if (x == null) {
            throw new IllegalArgumentException("Cannot add null to the deque.");
        }
        Node curr = new Node(x, sentinel, sentinel.prev);
        sentinel.prev.next = curr;
        sentinel.prev = curr;
        size += 1;
    }

    /**
     * Returns a List copy of the deque. Does not alter the deque.
     *
     * @return a new list copy of the deque.
     */
    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node current = sentinel.next; // Start from the first real node after the sentinel.
        while (current != sentinel) { // Traverse until we reach the sentinel again.
            list.add(current.value);
            current = current.next; // Move to the next node.
        }
        return list;
    }

    /**
     * Returns if the deque is empty. Does not alter the deque.
     *
     * @return {@code true} if the deque has no elements, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
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
            return null; // Deque is empty, nothing to remove.
        }
        Node firstNode = sentinel.next; // The first real node after the sentinel.
        sentinel.next = firstNode.next; // Update the sentinel's next pointer.
        firstNode.next.prev = sentinel; // Update the next node's previous pointer.
        size -= 1; // Decrease the size of the deque.
        return firstNode.value; // Return the value of the removed node.
    }

    /**
     * Remove and return the element at the back of the deque if it exists.
     *
     * @return removed element, otherwise {@code null}.
     */
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null; // Deque is empty, nothing to remove.
        }
        Node lastNode = sentinel.prev; // The last real node before the sentinel.
        sentinel.prev = lastNode.prev; // Update the sentinel's previous pointer.
        lastNode.prev.next = sentinel; // Update the previous node's next pointer.
        size -= 1; // Decrease the size of the deque.
        return lastNode.value; // Return the value of the removed node.
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
            return null; // index out of bounds.
        }
        Node current = sentinel.next; // Start from the first real node after the sentinel.
        for (int i = 0; i < index; i++) {
            current = current.next; // Traverse to the desired index.
        }
        return current.value; // Return the value at the specified index.
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
            return null; // index out of bounds.
        }
        return getRecursiveHelper(sentinel.next, index); // Start from the first real node after the sentinel.
    }
    private T getRecursiveHelper(Node current, int index) {
        if (index == 0) {
            return current.value; // Base case: return the value at the current node.
        }
        return getRecursiveHelper(current.next, index - 1); // Recursive case: move to the next node and decrease index.
    }

    /**
     * Inner class representing a node in the linked list.
     * It contains a value of type T, and pointers to the next and previous nodes.
     */
    private class Node { // cannot be static because it needs to access the generic type T
        // cannot use Optional<T> here because it would fail the autograder
        T value;
        Node next;
        Node prev;

        Node(T value) {
            this(value, null, null);
        }

        Node(T value, Node next, Node prev) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }
    }
}
