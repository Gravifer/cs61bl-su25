import com.google.common.truth.Ordered;
import org.junit.Test;

import java.util.Collections;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth.assertThat;


public class ListSetTest {

    @Test
    public void testBasics() {
        ListSet aSet = new ListSet();
        assertWithMessage("Size is not zero upon instantiation").that(aSet.size()).isEqualTo(0);
        for (int i = -50; i < 50; i += 2) {
            aSet.add(i);
            assertWithMessage("aSet should contain " + i).that(aSet.contains(i)).isTrue();
        }

        assertWithMessage("Size is not 50 after 50 calls to add").that(aSet.size()).isEqualTo(50);
        for (int i = -50; i < 50; i += 2) {
            aSet.remove(i);
            assertWithMessage("aSet should not contain " + i).that(aSet.contains(i)).isFalse();
        }

        assertWithMessage("aSet is not empty after removing all elements").that(aSet.isEmpty()).isTrue();
        assertWithMessage("Size is not zero after removing all elements").that(aSet.size()).isEqualTo(0);
    }

    @Test
    public void testRemove() {
        ListSet aSet = new ListSet();
        aSet.add(3);
        assertWithMessage("aSet should contain 3").that(aSet.contains(3));
        aSet.remove(3);
        assertWithMessage("aSet should not contain 3 after removal").that(!aSet.contains(3));
        assertWithMessage("Size should be zero after removing the only element").that(aSet.size()).isEqualTo(0);
    }

    @Test
    public void testRemoveNonExistent() {
        ListSet aSet = new ListSet();
        aSet.add(3);
        assertWithMessage("aSet should contain 3").that(aSet.contains(3));
        aSet.remove(5); // Attempt to remove an element not in the set
        assertWithMessage("aSet should still contain 3 after trying to remove 5").that(aSet.contains(3));
        assertWithMessage("Size should still be 1 after trying to remove a non-existent element").that(aSet.size()).isEqualTo(1);
    }

    @Test
    public void testAddDuplicates() {
        ListSet aSet = new ListSet();
        aSet.add(3);
        assertWithMessage("aSet should contain 3").that(aSet.contains(3)).isTrue();
        aSet.add(3); // Attempt to add a duplicate
        assertWithMessage("aSet should still contain 3 after trying to add it again").that(aSet.contains(3)).isTrue();
        assertWithMessage("Size should still be 1 after trying to add a duplicate").that(aSet.size()).isEqualTo(1);
    }

    @Test
    public void testToIntArray() {
        ListSet aSet = new ListSet();
        for (int i = 0; i < 5; i++) {
            aSet.add(i);
        }

        assertWithMessage("toIntArray should return the correct elements")
                .that(aSet.toIntArray()).asList()
                .containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    public void testIsEmpty() {
        ListSet aSet = new ListSet();
        assertWithMessage("aSet should be empty upon instantiation").that(aSet.isEmpty()).isTrue();
        aSet.add(1);
        assertWithMessage("aSet should not be empty after adding an element").that(aSet.isEmpty()).isFalse();
        aSet.remove(1);
        assertWithMessage("aSet should be empty after removing the only element").that(aSet.isEmpty()).isTrue();
    }

    @Test
    public void testContains() {
        ListSet aSet = new ListSet();
        aSet.add(10);
        assertWithMessage("aSet should contain 10").that(aSet.contains(10)).isTrue();
        assertWithMessage("aSet should not contain 20").that(aSet.contains(20)).isFalse();
        aSet.add(20);
        assertWithMessage("aSet should now contain 20").that(aSet.contains(20)).isTrue();
    }

    @Test
    public void testSizeAfterMultipleAddsAndRemoves() {
        ListSet aSet = new ListSet();
        for (int i = 0; i < 10; i++) {
            aSet.add(i);
        }
        assertWithMessage("Size should be 10 after adding 10 elements").that(aSet.size()).isEqualTo(10);

        for (int i = 0; i < 5; i++) {
            aSet.remove(i);
        }
        assertWithMessage("Size should be 5 after removing 5 elements").that(aSet.size()).isEqualTo(5);

        for (int i = 5; i < 10; i++) {
            aSet.remove(i);
        }
        assertWithMessage("Size should be 0 after removing all elements").that(aSet.size()).isEqualTo(0);
    }

    @Test
    public void testAddNegativeNumbers() {
        ListSet aSet = new ListSet();
        for (int i = -5; i <= 5; i++) {
            aSet.add(i);
        }
        assertWithMessage("Size should be 11 after adding numbers from -5 to 5").that(aSet.size()).isEqualTo(11);
        for (int i = -5; i <= 5; i++) {
            assertWithMessage("aSet should contain " + i).that(aSet.contains(i)).isTrue();
        }
    }
}
