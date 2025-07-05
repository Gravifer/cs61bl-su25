package deque;

import java.util.List;

/**
 * Created by hug on 2/4/2017. Methods are provided in the suggested order
 * that they should be completed.
 */
public interface Deque61B<T> extends Iterable<T> {

    /**
     * Add {@code x} to the front of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    void addFirst(T x);

    /**
     * Add {@code x} to the back of the deque. Assumes {@code x} is never null.
     *
     * @param x item to add
     */
    void addLast(T x);

    /**
     * Returns a List copy of the deque. Does not alter the deque.
     *
     * @return a new list copy of the deque.
     */
    List<T> toList();

    /**
     * Returns if the deque is empty. Does not alter the deque.
     *
     * @return {@code true} if the deque has no elements, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the size of the deque. Does not alter the deque.
     *
     * @return the number of items in the deque.
     */
    int size();

    /**
     * Remove and return the element at the front of the deque if it exists.
     *
     * @return removed element, otherwise {@code null}.
     */
    T removeFirst();

    /**
     * Remove and return the element at the back of the deque if it exists.
     *
     * @return removed element, otherwise {@code null}.
     */
    T removeLast();

    /** Returns a String representation of the Deque61B.
     * The format is: [item1, item2, ..., itemN]
     * where item1 is the first element and itemN is the last element.
     *
     * @return a String representation of the Deque61B
     */
    default String Deque61BToString() {
        if (isEmpty()) {
            return "[]"; // Return empty representation if deque is empty
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (T item : this) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(item);
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns whether this and the given deque or object are equal.
     * Two deques are considered equal if they have the same size and
     * all corresponding elements are equal.
     *
     * @param o object to compare with
     * @return {@code true} if this deque is equal to {@code o}, {@code false} otherwise
     */
    default boolean equalsDeque61B(Deque61B<?> o) {
        if (this == o) return true;
        if (this.size() != o.size()) return false;
        // compare type parameters

        var thisIterator = this.iterator();
        var thatIterator = o.iterator();
        while (thisIterator.hasNext() && thatIterator.hasNext()) {
            if (!thisIterator.next().equals(thatIterator.next())) {
                return false;
            }
        }
        return true;
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
    T get(int index);

    /**
     * This method technically shouldn't be in the interface, but it's here
     * to make testing nice. Gets an element, recursively. Returns null if
     * index is out of bounds. Does not alter the deque.
     *
     * @param index index to get
     * @return element at {@code index} in the deque
     */
    T getRecursive(int index);
}
