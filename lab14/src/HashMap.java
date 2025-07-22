import java.util.*;
import java.util.function.Consumer;

public class HashMap<K, V> implements Map61BL<K, V> {
    /* DONE: Instance variables here */
    private LinkedList<Entry61BL<K, V>>[] buckets;

    private int keyIndex(K key) {
        return Math.floorMod(key.hashCode(), buckets.length);
    }

    /* DONE: Constructors here */
    public HashMap() {
        this(16); // Default initial capacity
    }

    public HashMap(int initialCapacity) {
        buckets = new LinkedList[initialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            buckets[i] = new LinkedList<>(); // LinkedList<Entry<K, V>>();
        }
    }

    public HashMap(int i, int j) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /* DONE: Interface methods here */

    /**
     * Removes all of the mappings from this map.
     * <p>
     * This method calls {@code clear()} on each bucket to remove all entries from the map.
     */
    @Override
    public void clear() {
        for (LinkedList<Entry61BL<K, V>> bucket : buckets) {
            bucket.clear(); // Clear each bucket
        }
    }

    /**
     * @param key the key to check
     * @return true if this map contains a mapping for the specified {@code key}.
     */
    @Override
    public boolean containsKey(K key) {
        int index = keyIndex(key);
        LinkedList<Entry61BL<K, V>> bucket = buckets[index];
        for (Entry61BL<K, V> entry : bucket) {
            if (entry.keyEquals(new Entry61BL<>(key, null))) {
                return true; // Key found
            }
        }
        return false; // Key not found
    }

    /**
     * @param key the key to retrieve the value for
     * @return the value to which the specified key is mapped,
     * or null if this map contains no mapping for {@code key}.
     */
    @Override
    public V get(K key) {
        int index = keyIndex(key);
        LinkedList<Entry61BL<K, V>> bucket = buckets[index];
        for (Entry61BL<K, V> entry : bucket) {
            if (entry.keyEquals(new Entry61BL<>(key, null))) {
                return entry.value; // Return the value if key matches
            }
        }
        return null; // Key not found
    }

    /**
     * @param key   the key to put in the map
     * @param value the value to associate with the key
     */
    @Override
    public void put(K key, V value) {
        int index = keyIndex(key);
        LinkedList<Entry61BL<K, V>> bucket = buckets[index];

        // Check if the key already exists in the bucket
        for (Entry61BL<K, V> entry : bucket) {
            if (entry.keyEquals(new Entry61BL<>(key, null))) {
                entry.value = value; // Update the value if key exists
                return;
            }
        }

        // If key does not exist, add a new entry
        bucket.add(new Entry61BL<>(key, value));
        // and _then_ check if resizing is needed
        if (loadFactor() >= 0.75) {
            resize(ResizeDirective.UP); // Resize up if load factor exceeds threshold
            index = keyIndex(key); // Recalculate index after resizing
            bucket = buckets[index]; // Get the new bucket
        }
    }

    /**
     * @param key the key to remove
     * @return the value associated with the key if it was removed,
     * or null if the key was not found.
     */
    @Override
    public V remove(K key) {
        int index = keyIndex(key);
        LinkedList<Entry61BL<K, V>> bucket = buckets[index];

        // Iterate through the bucket to find the entry
        for (Entry61BL<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                bucket.remove(entry); // Remove the entry
                if (loadFactor() <= 0.25 && capacity() > 16) {
                    resize(ResizeDirective.DOWN); // Resize down if load factor is below threshold
                }
                return entry.value; // Return the value of the removed entry
            }
        }
        return null; // Key not found
    }

    /**
     * @param key   the key to remove
     * @param value the value to check for removal
     * @return true if the key-value pair was removed,
     * false otherwise.
     */
    @Override
    public boolean remove(K key, V value) {
        int index = keyIndex(key);
        LinkedList<Entry61BL<K, V>> bucket = buckets[index];

        // Iterate through the bucket to find the entry
        for (Entry61BL<K, V> entry : bucket) {
            if (entry.key.equals(key) && entry.value.equals(value)) {
                bucket.remove(entry); // Remove the entry if key and value match
                if (loadFactor() <= 0.25 && capacity() > 16) {
                    resize(ResizeDirective.DOWN); // Resize down if load factor is below threshold
                }
                return true; // Successfully removed
            }
        }
        return false; // Key-value pair not found
    }

    /**
     * @return the number of key-value pairs in this map.
     */
    @Override
    public int size() {
        int size = 0;
        for (LinkedList<Entry61BL<K, V>> bucket : buckets) {
            size += bucket.size(); // Sum the sizes of all buckets
        }
        return size;
    }

    /**
     * @return the number of buckets in this map, which is the capacity.
     */
    int capacity() {
        return buckets.length; // Return the number of buckets as the capacity
    }

    private double loadFactor() { // use 0.75 as threshold
        return size() / (double) capacity(); // Calculate load factor as size divided by capacity
    }

    private enum ResizeDirective {
        UP, DOWN
    }
    private boolean resize(ResizeDirective directive) {
        // // check if resizing is necessary
        // if ((directive == ResizeDirective.UP && loadFactor() < 0.75) ||
        //     (directive == ResizeDirective.DOWN && loadFactor() > 0.25)) {
        //     return false; // No resizing needed
        // }
        int newCapacity = (directive == ResizeDirective.UP) ? capacity() * 2 : capacity() / 2;
        LinkedList<Entry61BL<K, V>>[] newBuckets = new LinkedList[newCapacity];
        for (int i = 0; i < newCapacity; i++) {
            newBuckets[i] = new LinkedList<>(); // Initialize new buckets
        }

        // Rehash all entries into the new buckets
        for (LinkedList<Entry61BL<K, V>> bucket : buckets) {
            for (Entry61BL<K, V> entry : bucket) {
                int newIndex = Math.floorMod(entry.key.hashCode(), newCapacity);
                newBuckets[newIndex].add(entry); // Add entry to the new bucket
            }
        }
        buckets = newBuckets; // Update the buckets reference
        return true; // Resizing was successful
    }

    private class KeyIterator implements Iterator<K> {
        private int bucketIndex = 0;
        private Iterator<Entry61BL<K, V>> currentBucketIterator = buckets[bucketIndex].iterator();

        @Override
        public boolean hasNext() {
            while (bucketIndex < buckets.length) {
                if (currentBucketIterator.hasNext()) {
                    return true; // There are more elements in the current bucket
                }
                bucketIndex++;
                if (bucketIndex < buckets.length) {
                    currentBucketIterator = buckets[bucketIndex].iterator(); // Move to the next bucket
                }
            }
            return false; // No more elements in any bucket
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return currentBucketIterator.next().key; // Return the key of the next entry
        }
    }

    private class ValueIterator implements Iterator<V> {
        private int bucketIndex = 0;
        private Iterator<Entry61BL<K, V>> currentBucketIterator = buckets[bucketIndex].iterator();

        @Override
        public boolean hasNext() {
            while (bucketIndex < buckets.length) {
                if (currentBucketIterator.hasNext()) {
                    return true; // There are more elements in the current bucket
                }
                bucketIndex++;
                if (bucketIndex < buckets.length) {
                    currentBucketIterator = buckets[bucketIndex].iterator(); // Move to the next bucket
                }
            }
            return false; // No more elements in any bucket
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return currentBucketIterator.next().value; // Return the value of the next entry
        }
    }

    // @Override
    public Set<Entry61BL<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry61BL<K, V>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return size();
            }

            @Override
            public boolean contains(Object o) {
                return o instanceof HashMap.Entry61BL && contains(((Entry61BL<?, ?>) o).key);
            }
        };
    }
    private class EntryIterator implements Iterator<Entry61BL<K, V>> {
        private int bucketIndex = 0;
        private Iterator<Entry61BL<K, V>> currentBucketIterator = buckets[bucketIndex].iterator();

        @Override
        public boolean hasNext() {
            while (bucketIndex < buckets.length) {
                if (currentBucketIterator.hasNext()) {
                    return true; // There are more elements in the current bucket
                }
                bucketIndex++;
                if (bucketIndex < buckets.length) {
                    currentBucketIterator = buckets[bucketIndex].iterator(); // Move to the next bucket
                }
            }
            return false; // No more elements in any bucket
        }

        @Override
        public Entry61BL<K, V> next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return currentBucketIterator.next(); // Return the next entry
        }
    }

    private static class Entry61BL<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        Entry61BL(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /* Returns true if this key matches with the OTHER's key. */
        public boolean keyEquals(Entry61BL other) {
            return key.equals(other.key);
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *                               required to, throw this exception if the entry has been
         *                               removed from the backing map.
         */
        @Override
        public K getKey() {
            if (key == null) {
                throw new IllegalStateException("Entry has been removed from the backing map.");
            }
            return key;
        }

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * {@code remove} operation), the results of this call are undefined.
         *
         * @return the value corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *                               required to, throw this exception if the entry has been
         *                               removed from the backing map.
         */
        @Override
        public V getValue() {
            if (value == null) {
                throw new IllegalStateException("Entry has been removed from the backing map.");
            }
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation).  (Writes through to the map.)  The
         * behavior of this call is undefined if the mapping has already been
         * removed from the map (by the iterator's {@code remove} operation).
         *
         * @param value new value to be stored in this entry
         * @return old value corresponding to the entry
         * @throws UnsupportedOperationException if the {@code put} operation
         *                                       is not supported by the backing map
         * @throws ClassCastException            if the class of the specified value
         *                                       prevents it from being stored in the backing map
         * @throws NullPointerException          if the backing map does not permit
         *                                       null values, and the specified value is null
         * @throws IllegalArgumentException      if some property of this value
         *                                       prevents it from being stored in the backing map
         * @throws IllegalStateException         implementations may, but are not
         *                                       required to, throw this exception if the entry has been
         *                                       removed from the backing map.
         */
        @Override
        public V setValue(V value) {
            if (this.value == null) {
                throw new IllegalStateException("Entry has been removed from the backing map.");
            }
            V oldValue = this.value; // Store the old value
            this.value = value; // Update the value
            return oldValue; // Return the old value
        }

        /* Returns true if both the KEY and the VALUE match. */
        @Override
        public boolean equals(Object other) {
            return (other instanceof HashMap.Entry61BL
                    && key.equals(((Entry61BL) other).key)
                    && value.equals(((Entry61BL) other).value));
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    /**
     * @return an Iterator over the keys in this map.
     */
    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(Consumer<? super K> action) {
        Objects.requireNonNull(action, "Action must not be null");
        for (K key : this) {
            action.accept(key); // Apply the action to each key
        }
    }

    private class KeySpliterator implements Spliterator<K> {
        private int bucketIndex = 0;
        private Iterator<Entry61BL<K, V>> currentBucketIterator = buckets[bucketIndex].iterator();

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            Objects.requireNonNull(action, "Action must not be null");
            while (bucketIndex < buckets.length) {
                if (currentBucketIterator.hasNext()) {
                    action.accept(currentBucketIterator.next().key); // Apply action to the key
                    return true; // Successfully advanced
                }
                bucketIndex++;
                if (bucketIndex < buckets.length) {
                    currentBucketIterator = buckets[bucketIndex].iterator(); // Move to the next bucket
                }
            }
            return false; // No more elements to advance
        }

        @Override
        public Spliterator<K> trySplit() {
            if (bucketIndex >= buckets.length - 1) {
                return null; // No more buckets to split
            }
            int mid = (buckets.length + bucketIndex) / 2; // Calculate the midpoint for splitting
            KeySpliterator newSpliterator = new KeySpliterator();
            newSpliterator.bucketIndex = mid; // Set the starting index for the new spliterator
            newSpliterator.currentBucketIterator = buckets[mid].iterator(); // Start from the midpoint bucket
            return newSpliterator; // Return the new spliterator
        }

        @Override
        public long estimateSize() {
            long size = 0;
            for (LinkedList<Entry61BL<K, V>> bucket : buckets) {
                size += bucket.size(); // Sum the sizes of all buckets
            }
            return size; // Return the total size
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL; // Characteristics of this spliterator
        }
    }
    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="../util/Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public Spliterator<K> spliterator() {
        return new KeySpliterator();
    }
}
