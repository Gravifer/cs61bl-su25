public class BinarySearchTree<T extends Comparable<T>> extends BinaryTree<T> {

    /* Creates an empty BST. Super() calls the constructor for BinaryTree (not in scope). */
    public BinarySearchTree() {
        super();
    }

    /* Creates a BST with root as ROOT. */
    public BinarySearchTree(TreeNode root) {
        super(root);
    }

    /* Returns true if the BST contains the given KEY. */
    public boolean contains(T key) {
        // DONE: YOUR CODE HERE: an extra helper method might be useful // * except I didn't use recursion
        TreeNode<T> curr = root;
        while (curr != null) {
            if (curr.item.equals(key)) {
                return true;
            } else if (curr.item.compareTo(key) > 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }
        return false;
    }

    /* Adds a node for KEY iff KEY isn't in the BST already. */
    public void add(T key) {
        // DONE: YOUR CODE HERE: an extra helper method might be useful
        TreeNode<T> newNode = new TreeNode<>(key);
        if (root == null) {
            root = newNode; // If the tree is empty, set the new node as root
            return;
        }
        TreeNode<T> curr = root;
        TreeNode<T> parent = null;
        while (curr != null) {
            parent = curr;
            if (curr.item.compareTo(key) > 0) {
                curr = curr.left; // Go left
            } else if (curr.item.compareTo(key) < 0) {
                curr = curr.right; // Go right
            } else {
                return; // Key already exists, do not add
            }
        }
        // Insert the new node as a child of the parent
        if (parent.item.compareTo(key) > 0) {
            parent.left = newNode; // Add as left child
        } else {
            parent.right = newNode; // Add as right child
        }
    }

    /* Deletes a node from the BST. 
     * Even though you do not have to implement delete, you 
     * should read through and understand the basic steps.
    */
    public T delete(T key) {
        TreeNode<T> parent = null;
        TreeNode<T> curr = root;
        TreeNode<T> delNode = null;
        TreeNode<T> replacement = null;
        boolean rightSide = false;

        while (curr != null && !curr.item.equals(key)) {
            if (curr.item.compareTo(key) > 0) {
                parent = curr;
                curr = curr.left;
                rightSide = false;
            } else {
                parent = curr;
                curr = curr.right;
                rightSide = true;
            }
        }
        delNode = curr;
        if (curr == null) {
            return null;
        }

        if (delNode.right == null) {
            if (root == delNode) {
                root = root.left;
            } else {
                if (rightSide) {
                    parent.right = delNode.left;
                } else {
                    parent.left = delNode.left;
                }
            }
        } else {
            curr = delNode.right;
            replacement = curr.left;
            if (replacement == null) {
                replacement = curr;
            } else {
                while (replacement.left != null) {
                    curr = replacement;
                    replacement = replacement.left;
                }
                curr.left = replacement.right;
                replacement.right = delNode.right;
            }
            replacement.left = delNode.left;
            if (root == delNode) {
                root = replacement;
            } else {
                if (rightSide) {
                    parent.right = replacement;
                } else {
                    parent.left = replacement;
                }
            }
        }
        return delNode.item;
    }
}
