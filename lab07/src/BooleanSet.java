/**
 * Represent a set of nonnegative ints from 0 to maxElement (inclusive) for some initially
 * specified maxElement.
 */
public class BooleanSet implements SimpleSet {

    private final boolean[] contains;
    private int size, maxElement;

    /** Initializes a set of ints from 0 to maxElement. */
    public BooleanSet(int maxElement) {
        contains = new boolean[maxElement + 1];
        size = 0;
        this.maxElement = maxElement;
    }

    /** Adds k to the set. */
    @Override
    public void add(int k) {
        if (k < 0 || k > maxElement) {
            throw new IllegalArgumentException("Element out of bounds: " + k);
        }
        if (!contains[k]) {
            contains[k] = true;
            size++;
        }
    }

    /** Removes k from the set. */
    @Override
    public void remove(int k) {
        if (k < 0 || k > maxElement) {
            throw new IllegalArgumentException("Element out of bounds: " + k);
        }
        if (contains[k]) {
            contains[k] = false;
            size--;
        }
    }

    /** Return true if k is in this set, false otherwise. */
    @Override
    public boolean contains(int k) {
        return contains[k];
    }


    /** Return true if this set is empty, false otherwise. */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /** Returns the number of items in the set. */
    @Override
    public int size() {
        return size;
    }

    /** Returns an array containing all of the elements in this collection. */
    @Override
    public int[] toIntArray() {
        int[] result = new int[size];
        int index = 0;
        for (int i = 0; i <= maxElement; i++) {
            if (contains[i]) {
                result[index++] = i;
            }
        }
        return result;
    }
}
