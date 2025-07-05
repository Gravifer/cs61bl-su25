import deque.ArrayDeque61B;

import jh61b.utils.Reflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class ArrayDeque61BTest {

    @Test
    @DisplayName("ArrayDeque61B has no fields besides backing array and primitives")
    void noNonTrivialFields() {
        List<Field> badFields = Reflection.getFields(ArrayDeque61B.class)
                .filter(f -> !(f.getType().isPrimitive() || f.getType().equals(Object[].class) || f.isSynthetic()))
                .toList();

        assertWithMessage("Found fields that are not array or primitives").that(badFields).isEmpty();
    }

    @Test
    void testAddFirst() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addFirst(1);
        assertThat(deque.toList()).containsExactly(1).inOrder();

        deque.addFirst(2);
        assertThat(deque.toList()).containsExactly(2, 1).inOrder();
    }

    @Test
    void testAddLast() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        assertThat(deque.toList()).containsExactly(1).inOrder();

        deque.addLast(2);
        assertThat(deque.toList()).containsExactly(1, 2).inOrder();
    }

    @Test
    void testRemoveFirst() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addFirst(1);
        deque.addFirst(2);

        Integer removed = deque.removeFirst();
        assertThat(removed).isEqualTo(2);
        assertThat(deque.toList()).containsExactly(1).inOrder();

        removed = deque.removeFirst();
        assertThat(removed).isEqualTo(1);
        assertThat(deque.isEmpty()).isTrue();
    }

    @Test
    void testRemoveLast() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);

        Integer removed = deque.removeLast();
        assertThat(removed).isEqualTo(2);
        assertThat(deque.toList()).containsExactly(1).inOrder();

        removed = deque.removeLast();
        assertThat(removed).isEqualTo(1);
        assertThat(deque.isEmpty()).isTrue();
    }

    @Test
    void testToList() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.toList()).isEmpty();

        deque.addFirst(1);
        deque.addLast(2);
        deque.addFirst(0);

        List<Integer> expectedList = List.of(0, 1, 2);
        assertThat(deque.toList()).containsExactlyElementsIn(expectedList).inOrder();
    }

    @Test
    void testIsEmptyAndSize() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.isEmpty()).isTrue();
        assertThat(deque.size()).isEqualTo(0);

        deque.addFirst(1);
        assertThat(deque.isEmpty()).isFalse();
        assertThat(deque.size()).isEqualTo(1);

        deque.addLast(2);
        assertThat(deque.isEmpty()).isFalse();
        assertThat(deque.size()).isEqualTo(2);

        deque.removeFirst();
        assertThat(deque.size()).isEqualTo(1);

        deque.removeLast();
        assertThat(deque.isEmpty()).isTrue();
    }

    @Test
    void testAddNullThrowsException() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        try {
            deque.addLast(null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Cannot add null to the deque");
        }
    }

    @Test
    void testRemoveFromEmptyDeque() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.removeFirst()).isNull();
        assertThat(deque.removeLast()).isNull();
    }

    @Test
    void testSizeAfterOperations() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.size()).isEqualTo(0);

        deque.addFirst(1);
        assertThat(deque.size()).isEqualTo(1);

        deque.addLast(2);
        assertThat(deque.size()).isEqualTo(2);

        deque.removeFirst();
        assertThat(deque.size()).isEqualTo(1);

        deque.removeLast();
        assertThat(deque.size()).isEqualTo(0);
    }

    @Test
    void testIsEmptyAfterOperations() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.isEmpty()).isTrue();

        deque.addFirst(1);
        assertThat(deque.isEmpty()).isFalse();

        deque.addLast(2);
        assertThat(deque.isEmpty()).isFalse();

        deque.removeFirst();
        assertThat(deque.isEmpty()).isFalse();

        deque.removeLast();
        assertThat(deque.isEmpty()).isTrue();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class ArrayDeque61BInspectLength<T> extends  ArrayDeque61B<T> {
        public int getBackingArrayLength() {
            return super.getBackingArrayLength(); // Expose the length of the backing array for testing
        }
    }

    /**
     * Test for resizing the deque when adding elements beyond initial capacity.
     * This ensures that the deque can grow and shrink dynamically as needed.
     */
    @Test
    void testResize() {
        ArrayDeque61BInspectLength<Integer> deque = new ArrayDeque61BInspectLength<>();
        for (int i = 0; i < 200; i++) {
            deque.addLast(i);
        }
        assertThat(deque.size()).isEqualTo(200);
        assertThat(deque.getBackingArrayLength() > 200).isTrue(); // Ensure the backing array has grown

        for (int i = 0; i < 180; i++) {
            deque.removeFirst();
        }
        assertThat(deque.size()).isEqualTo(20);
        assertThat(deque.getBackingArrayLength() < 80).isTrue(); // Ensure the backing array has shrunk
    }

    @Test
    void testGetMethod() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);

        assertThat(deque.get(0)).isEqualTo(1);
        assertThat(deque.get(1)).isEqualTo(2);
        assertThat(deque.get(2)).isEqualTo(3);

        try {
            deque.get(-1);
        } catch (IndexOutOfBoundsException e) {
            assertThat(e).hasMessageThat().contains("Index out of bounds");
        }

        try {
            deque.get(3);
        } catch (IndexOutOfBoundsException e) {
            assertThat(e).hasMessageThat().contains("Index out of bounds");
        }
    }

    @Test
    void testGetFromEmptyDeque() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.get(0)).isNull(); // Should return null for empty deque
    }

    @Test
    void testGetWithInvalidIndex() { // should return null for invalid indices
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);

        assertThat(deque.get(-1)).isNull(); // Negative index
        assertThat(deque.get(2)).isNull(); // Index out of bounds
    }

    @Test
    void testGetRecursive() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);

        assertThat(deque.get(0)).isEqualTo(1);
        assertThat(deque.get(1)).isEqualTo(2);
        assertThat(deque.get(2)).isEqualTo(3);

        // Test with invalid indices
        assertThat(deque.get(-1)).isNull(); // Negative index
        assertThat(deque.get(3)).isNull(); // Index out of bounds
    }

    @Test
    void testEqualsAndHashCode() {
        ArrayDeque61B<Integer> deque1 = new ArrayDeque61B<>();
        ArrayDeque61B<Integer> deque2 = new ArrayDeque61B<>();

        deque1.addLast(1);
        deque1.addLast(2);
        deque2.addLast(1);
        deque2.addLast(2);

        assertThat(deque1).isEqualTo(deque2);
        assertThat(deque1.hashCode()).isEqualTo(deque2.hashCode());

        deque2.addLast(3);
        assertThat(deque1).isNotEqualTo(deque2);
    }

    @Test
    void testToString() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        assertThat(deque.toString()).isEqualTo("[]");

        deque.addLast(1);
        assertThat(deque.toString()).isEqualTo("[1]");

        deque.addLast(2);
        assertThat(deque.toString()).isEqualTo("[1, 2]");

        deque.addFirst(0);
        assertThat(deque.toString()).isEqualTo("[0, 1, 2]");
    }

    @Test
    void testIterator() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);

        assertThat(deque).containsExactly(1, 2, 3).inOrder();

        // Test iterator after modifications
        deque.removeFirst();
        assertThat(deque).containsExactly(2, 3).inOrder();
    }

    @Test
    @DisplayName("ArrayDeque61B handles null elements correctly")
    void handlesNullElements() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        try {
            deque.addFirst(null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Cannot add null to the deque");
        }

        try {
            deque.addLast(null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Cannot add null to the deque");
        }
    }

    @Test
    @DisplayName("ArrayDeque61B maintains order of elements")
    void maintainsOrder() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        deque.addLast(1);
        deque.addLast(2);
        deque.addFirst(0);

        List<Integer> expectedList = List.of(0, 1, 2);
        assertThat(deque.toList()).containsExactlyElementsIn(expectedList).inOrder();

        deque.removeFirst();
        expectedList = List.of(1, 2);
        assertThat(deque.toList()).containsExactlyElementsIn(expectedList).inOrder();

        deque.removeLast();
        expectedList = List.of(1);
        assertThat(deque.toList()).containsExactlyElementsIn(expectedList).inOrder();
    }

}
