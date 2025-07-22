// Map61BL.java
import java.util.Iterator;

public interface Map61BL<K, V> extends Iterable<K> {

    /* Removes all of the mappings from this map. */
    void clear();

    /* Returns true if this map contains a mapping for the specified {@code key}. */
    boolean containsKey(K key);

    /* Returns the value to which the specified {@code key} is mapped, or null if
     * this map contains no mapping for KEY. */
    V get(K key);

    /* Puts the specified key-value pair {@code (key, value)} in this map. */
    void put(K key, V value);

    /* Removes and returns a {@code key} and its associated value. */
    V remove(K key);

    /* Removes a particular key-value pair {@code (key, value)} and returns true if
     * successful. */
    boolean remove(K key, V value);

    /* Returns the number of key-value pairs in this map. */
    int size();

    /* Returns an Iterator over the keys in this map. */
    Iterator<K> iterator();
}
