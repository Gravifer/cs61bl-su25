import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class BinarySearchTreeTest {

    // TODO: add some of your own tests here to test your implementation!

    @Test
    public void containsTest() {
        BinarySearchTree<Integer> x = new BinarySearchTree();
        assertWithMessage("BST should not contain anything immediately after instantiation").that(x.contains(2)).isFalse();
        x.add(2);
        assertWithMessage("BST should contain 2 after adding 2").that(x.contains(2)).isTrue();
        x.add(3);
        assertWithMessage("BST should contain 3 after adding 3").that(x.contains(3)).isTrue();
        x.add(4);
        assertWithMessage("BST should contain 4 after adding 4").that(x.contains(4)).isTrue();
        x.add(2);
        x.delete(2);
        assertWithMessage("BST should not contain 2 after deleting 2").that(x.contains(2)).isFalse();
    }

    @Test
    public void addTest() {
        BinarySearchTree<Integer> x = new BinarySearchTree();
        assertWithMessage("BST should not contain 5 before adding it").that(x.contains(5)).isFalse();
        x.add(5);
        assertWithMessage("BST should contain 5 after adding it").that(x.contains(5)).isTrue();
        x.add(6);
        assertWithMessage("BST should contain 6 after adding it").that(x.contains(6)).isTrue();
        x.add(7);
        assertWithMessage("BST should contain 7 after adding it").that(x.contains(7)).isTrue();
    }

    @Test
    public void deleteTest() {
        BinarySearchTree<Integer> x = new BinarySearchTree();
        x.add(10);
        x.add(20);
        x.add(30);
        assertWithMessage("BST should contain 20 before deleting it").that(x.contains(20)).isTrue();
        x.delete(20);
        assertWithMessage("BST should not contain 20 after deleting it").that(x.contains(20)).isFalse();
        assertWithMessage("BST should still contain 10 after deleting 20").that(x.contains(10)).isTrue();
        assertWithMessage("BST should still contain 30 after deleting 20").that(x.contains(30)).isTrue();
    }

    @Test
    public void heightGrowthTest() {
        // add sortable elements out of order
        BinarySearchTree<Integer> x = new BinarySearchTree();
        assertWithMessage("Height of empty BST should be 0").that(x.height()).isEqualTo(0);
        x.add(10);
        assertWithMessage("Height of BST with one element should be 1").that(x.height()).isEqualTo(1);
        x.add(5);
        assertWithMessage("Height of BST with two elements should be 2").that(x.height()).isEqualTo(2);
        x.add(15);
        assertWithMessage("Height of BST with three elements should be 2").that(x.height()).isEqualTo(2);
        x.add(3);
        assertWithMessage("Height of BST with four elements should be 3").that(x.height()).isEqualTo(3);
        x.add(7);
        assertWithMessage("Height of BST with five elements should be 3").that(x.height()).isEqualTo(3);
        x.add(12);
        assertWithMessage("Height of BST with six elements should be 3").that(x.height()).isEqualTo(3);
        // delete some elements
        x.delete(3);
        assertWithMessage("Height of BST after deleting 3 should still be 3").that(x.height()).isEqualTo(3);
        x.delete(7);
        assertWithMessage("Height of BST after deleting 7 should still be 3").that(x.height()).isEqualTo(3);
        x.add(3);
        x.delete(12);
        assertWithMessage("Height of BST after deleting 12 should still be 3").that(x.height()).isEqualTo(3);
    }
}
