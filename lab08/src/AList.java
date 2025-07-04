import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An AList is a list of integers. Like SLList, it also hides the terrible
 * truth of the nakedness within, but uses an array as its base.
 */
public class AList<Item> implements Iterable<Item> {
    /* TODO: Make AList able to be iterated over. Add new nested classes as necessary.
    *   Your code will likely not compile on the autograder unless you implement this section.*/

    private Item[] items;
    private int size;

    /** Creates an empty AList. */
    @SuppressWarnings("unchecked")
    public AList() {
        // The line below gives a warning (Unchecked cast), but you can ignore this.
        items = (Item[]) new Object[8];
        size = 0;
    }

    /** Returns a AList consisting of the given values. */
    @SafeVarargs
    public static <Item> AList<Item> of(Item... values) {
        AList<Item> list = new AList<>();
        for (Item value : values) {
            list.addLast(value);
        }
        return list;
    }

    /** Returns the size of the list. */
    public int size() {
        return size;
    }

    /** Adds item to the back of the list. */
    public void addLast(Item item) {
        if (items.length == size) {
            resize();
        }
        items[size] = item;
        size += 1;
    }

    /** Resize the array to accommodate more items. */
    @SuppressWarnings("unchecked")
    private void resize() {
        Item[] newItems = (Item[]) new Object[items.length * 2];
        System.arraycopy(items, 0, newItems, 0, size);
        items = newItems;
    }

    /** Returns the representation of the AList as a String. */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        int p = 0;
        boolean first = true;
        while (p != size) {
            if (first) {
                result.append(items[p].toString());
                first = false;
            } else {
                result.append(" ").append(items[p].toString());
            }
            p += 1;
        }
        return result.toString();
    }


    /** Returns whether this and the given list or object are equal. */
    public boolean equals(Object o) {
        if (o instanceof AList<?> other) {
            if (this.size != other.size) {
                return false;
            }
        } else {
            return false;
        }
        return Arrays.deepEquals(items, other.getItems());
    }

    /** Returns the underlying items array. */
    private Item[] getItems() {
        return items;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Item> iterator() {
        return new AListIterator();
    }

    private class AListIterator implements Iterator<Item> {
        private int currentIndex = 0;

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the AList iterator.");
            }
            Item item = items[currentIndex];
            currentIndex++;
            return item;
        }
    }
}
