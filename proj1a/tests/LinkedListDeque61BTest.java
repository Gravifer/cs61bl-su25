// coverage goals: https://cs61bl.org/su25/projects/proj1a/flags/

import jh61b.utils.Reflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/** Performs some basic linked list tests. */
public class LinkedListDeque61BTest {

    @Test
    /** In this test, we have three different assert statements that verify that addFirst works correctly. */
    public void addFirstTestBasic() {
        Deque61B<String> lld1 = new LinkedListDeque61B<>();

        lld1.addFirst("back"); // after this call we expect: ["back"]
        assertThat(lld1.toList()).containsExactly("back").inOrder();

        lld1.addFirst("middle"); // after this call we expect: ["middle", "back"]
        assertThat(lld1.toList()).containsExactly("middle", "back").inOrder();

        lld1.addFirst("front"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.toList()).containsExactly("front", "middle", "back").inOrder();

        /* Note: The first two assertThat statements aren't really necessary. For example, it's hard
           to imagine a bug in your code that would lead to ["front"] and ["front", "middle"] failing,
           but not ["front", "middle", "back"].
         */
    }

    @Test
    /** In this test, we use only one assertThat statement. IMO this test is just as good as addFirstTestBasic.
     *  In other words, the tedious work of adding the extra assertThat statements isn't worth it. */
    public void addLastTestBasic() {
        Deque61B<String> lld1 = new LinkedListDeque61B<>();

        lld1.addLast("front"); // after this call we expect: ["front"]
        lld1.addLast("middle"); // after this call we expect: ["front", "middle"]
        lld1.addLast("back"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.toList()).containsExactly("front", "middle", "back").inOrder();
    }

    @Test
    /** This test performs interspersed addFirst and addLast calls. */
    public void addFirstAndAddLastTest() {
        Deque61B<Integer> lld1 = new LinkedListDeque61B<>();

        /* I've decided to add in comments the state after each call for the convenience of the
           person reading this test. Some programmers might consider this excessively verbose. */
        lld1.addLast(0);   // [0]
        lld1.addLast(1);   // [0, 1]
        lld1.addFirst(-1); // [-1, 0, 1]
        lld1.addLast(2);   // [-1, 0, 1, 2]
        lld1.addFirst(-2); // [-2, -1, 0, 1, 2]

        assertThat(lld1.toList()).containsExactly(-2, -1, 0, 1, 2).inOrder();
    }

    // Below, you'll write your own tests for LinkedListDeque61B.
    @Test
    public void testSizeAndIsEmpty() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);

        lld.addFirst(1);
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);

        lld.addLast(2);
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(2);

        lld.removeFirst();
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);

        lld.removeLast();
        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);
    }

    @Test
    public void testRemoveFirstAndLast() {
        Deque61B<String> lld = new LinkedListDeque61B<>();
        lld.addFirst("A");
        lld.addLast("B");
        lld.addFirst("C");

        assertThat(lld.removeFirst()).isEqualTo("C"); // Should remove "C"
        assertThat(lld.removeLast()).isEqualTo("B");  // Should remove "B"
        assertThat(lld.removeFirst()).isEqualTo("A"); // Should remove "A"
        assertThat(lld.removeFirst()).isNull();       // Should return null since deque is empty
    }

    @Test
    public void testToList() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        lld.addFirst(1);
        lld.addLast(2);
        lld.addFirst(0);

        List<Integer> expectedList = List.of(0, 1, 2);
        assertThat(lld.toList()).containsExactlyElementsIn(expectedList).inOrder();
    }

    @Test
    public void testEmptyDeque() {
        Deque61B<String> lld = new LinkedListDeque61B<>();
        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);
        assertThat(lld.removeFirst()).isNull(); // Should return null
        assertThat(lld.removeLast()).isNull();  // Should return null
    }

    @Test
    public void testRemoveFirstToEmptyDeque() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        lld.addFirst(1);
        lld.addLast(2);
        lld.removeFirst(); // Now deque should have one element
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);
        assertThat(lld.toList()).containsExactly(2).inOrder();
        lld.removeFirst(); // Now deque should be empty
        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);
        lld.addFirst(0);
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);
    }

    @Test
    public void testRemoveLastToEmptyDeque() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        lld.addFirst(1);
        lld.addLast(2);
        lld.removeLast(); // Now deque should have one element
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);
        assertThat(lld.toList()).containsExactly(1).inOrder();
        lld.removeLast(); // Now deque should be empty
        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);
        lld.addLast(0);
        assertThat(lld.isEmpty()).isFalse();
        assertThat(lld.size()).isEqualTo(1);
    }

    @Test
    public void testAddAndRemoveMultipleElements() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        for (int i = 0; i < 10; i++) {
            lld.addLast(i);
        }

        assertThat(lld.size()).isEqualTo(10);
        assertThat(lld.isEmpty()).isFalse();

        for (int i = 0; i < 10; i++) {
            assertThat(lld.removeFirst()).isEqualTo(i);
        }

        assertThat(lld.isEmpty()).isTrue();
        assertThat(lld.size()).isEqualTo(0);
    }

    @Test
    public void testAddFirstAndLastMixed() {
        Deque61B<String> lld = new LinkedListDeque61B<>();
        lld.addFirst("A");
        lld.addLast("B");
        lld.addFirst("C");
        lld.addLast("D");

        assertThat(lld.toList()).containsExactly("C", "A", "B", "D").inOrder();
        assertThat(lld.size()).isEqualTo(4);
    }

    @Test
    public void testGenericType() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        lld.addFirst(1);
        lld.addLast(2);
        assertThat(lld.toList()).containsExactly(1, 2).inOrder();


        Deque61B<String> slld = new LinkedListDeque61B<>();
        slld.addFirst("Hello");
        slld.addLast("World");
        assertThat(slld.toList()).containsExactly("Hello", "World").inOrder();
    }


    @Test
    public void testRemoveFromEmptyDeque() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        assertThat(lld.removeFirst()).isNull(); // Should return null
        assertThat(lld.removeLast()).isNull();  // Should return null
    }

    @Test
    public void testAddNullThrowsException() { // both NullPointerException and IllegalArgumentException are acceptable
        Deque61B<Integer> lld = new LinkedListDeque61B<>();
        try {
            lld.addFirst(null);
            assertWithMessage("Adding null to the deque should throw an exception").fail();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected behavior
        }

        try {
            lld.addLast(null);
            assertWithMessage("Adding null to the deque should throw an exception").fail();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected behavior
        }
    }

    @Test
    public void testGetMethod() {
        Deque61B<String> lld = new LinkedListDeque61B<>();
        lld.addLast("A");
        lld.addLast("B");
        lld.addLast("C");

        assertThat(lld.get(0)).isEqualTo("A"); // First element
        assertThat(lld.get(1)).isEqualTo("B"); // Second element
        assertThat(lld.get(2)).isEqualTo("C"); // Third element
        assertThat(lld.get(3)).isNull();       // Out of bounds, should return null
        assertThat(lld.get(-1)).isNull();      // Negative index, should return null
    }

    @Test
    public void testGetRecursiveMethod() {
        Deque61B<String> lld = new LinkedListDeque61B<>();
        lld.addLast("A");
        lld.addLast("B");
        lld.addLast("C");

        assertThat(lld.getRecursive(0)).isEqualTo("A"); // First element
        assertThat(lld.getRecursive(1)).isEqualTo("B"); // Second element
        assertThat(lld.getRecursive(2)).isEqualTo("C"); // Third element
        assertThat(lld.getRecursive(3)).isNull();       // Out of bounds, should return null
        assertThat(lld.getRecursive(-1)).isNull();      // Negative index, should return null
    }
}
