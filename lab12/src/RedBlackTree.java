public class RedBlackTree<T extends Comparable<T>> {

    /* Root of the tree. */
    RBTreeNode<T> root;

    static class RBTreeNode<T> {

        final T item;
        boolean isBlack;
        RBTreeNode<T> left;
        RBTreeNode<T> right;

        /* Creates a RBTreeNode with item ITEM and color depending on ISBLACK
           value. */
        RBTreeNode(boolean isBlack, T item) {
            this(isBlack, item, null, null);
        }

        /* Creates a RBTreeNode with item ITEM, color depending on ISBLACK
           value, left child LEFT, and right child RIGHT. */
        RBTreeNode(boolean isBlack, T item, RBTreeNode<T> left,
                   RBTreeNode<T> right) {
            this.isBlack = isBlack;
            this.item = item;
            this.left = left;
            this.right = right;
        }
    }

    /* Creates an empty RedBlackTree. */
    public RedBlackTree() {
        root = null;
    }

    /* Creates a RedBlackTree from a given 2-3 TREE. */
    public RedBlackTree(TwoThreeTree<T> tree) {
        Node<T> ttTreeRoot = tree.root;
        root = buildRedBlackTree(ttTreeRoot);
    }

    /* Builds a RedBlackTree that has isometry with given 2-3 tree rooted at
       given node R, and returns the root node. */
    RBTreeNode<T> buildRedBlackTree(Node<T> r) {
        if (r == null) {
            return null;
        }

        if (r.getItemCount() == 1) {
            // done: Replace with code to create a 2-node equivalent
            return new RBTreeNode<>(true, r.getItemAt(0),
                    buildRedBlackTree(r.getChildAt(0)),
                    buildRedBlackTree(r.getChildAt(1)));
        } else {
            // DONE: Replace with code to create a 3-node equivalent
            RBTreeNode<T> leftChild = buildRedBlackTree(r.getChildAt(0));
            RBTreeNode<T> middleChild = buildRedBlackTree(r.getChildAt(1));
            RBTreeNode<T> redNode = new RBTreeNode<>(false, r.getItemAt(0),
                    leftChild, middleChild);
            RBTreeNode<T> rightChild = buildRedBlackTree(r.getChildAt(2));
            return new RBTreeNode<>(true, r.getItemAt(1),
                    redNode, rightChild);
        }
    }

    /**
     * Flips the color of the node and its children. Assume that NODE has both left
     * and right children
     * @param node the node whose color and the colors of its children will be flipped
     */
    void flipColors(RBTreeNode<T> node) {
        // DONE: YOUR CODE HERE
        if (node == null || node.left == null || node.right == null) {
            return; // Cannot flip colors if node or its children are null
        }
        // Swap colors of node and its children
        node.isBlack = !node.isBlack; // Flip the color of the current node
        node.left.isBlack = !node.left.isBlack; // Flip the color of the left child
        node.right.isBlack = !node.right.isBlack; // Flip the color of the right child
    }

    /**
     * Rotates the given node to the right. Returns the new root node of
     * this subtree. For this implementation, make sure to swap the colors
     * of the new root and the old root!
     * @param node the node to rotate right
     * @return the new root node of the subtree
     */
    RBTreeNode<T> rotateRight(RBTreeNode<T> node) {
        // DONE: YOUR CODE HERE
        if (node == null || node.left == null) {
            return node; // Cannot rotate right if there's no left child
        }
        RBTreeNode<T> newRoot = node.left;

        node.left = newRoot.right;
        newRoot.right = node;

        boolean tempColor = node.isBlack;
        node.isBlack = newRoot.isBlack;
        newRoot.isBlack = tempColor;

        // Swap colors, using xor; but do see https://codingwiththomas.blogspot.com/2012/10/java-xor-swap-performance.html
        // node.isBlack = node.isBlack ^ newRoot.isBlack;
        // newRoot.isBlack = node.isBlack ^ newRoot.isBlack;
        // node.isBlack = node.isBlack ^ newRoot.isBlack;

        return newRoot;
    }

    /**
     * Rotates the given node to the left. Returns the new root node of
     * this subtree. For this implementation, make sure to swap the colors
     * of the new root and the old root!
     * @param node the node to rotate left
     * @return the new root node of the subtree
     */
    RBTreeNode<T> rotateLeft(RBTreeNode<T> node) {
        // DONE: YOUR CODE HERE
        if (node == null || node.right == null) {
            return node;
        }
        RBTreeNode<T> newRoot = node.right;

        node.right = newRoot.left;
        newRoot.left = node;

        // For booleans, another way using xor
        node.isBlack = node.isBlack != newRoot.isBlack;
        newRoot.isBlack = node.isBlack != newRoot.isBlack;
        node.isBlack = node.isBlack != newRoot.isBlack;

        return newRoot;
    }

    /**
     * Inserts the item into the Red Black Tree. Colors the root of the tree black.
     * @param item the item to insert into the tree
     */
    public void insert(T item) {
        root = insert(root, item);
        root.isBlack = true;
    }

    /**
     * Inserts the given node into this Red Black Tree. Comments have been provided to help break
     * down the problem. For each case, consider the scenario needed to perform those operations.
     * Make sure to also review the other methods in this class!
     * @param node the node to insert into the tree
     * @param item the item to insert into the tree
     * @return the new root of the subtree after insertion
     */
    private RBTreeNode<T> insert(RBTreeNode<T> node, T item) {
        // DONE: Insert (return) new red leaf node.
        if (node == null) {
            return new RBTreeNode<>(false, item);
        }

        // DONE: Handle normal binary search tree insertion. The below line may help.
        int comp = item.compareTo(node.item);
        if (comp < 0) {
            node.left = insert(node.left, item);
        } else if (comp == 0) {
            return node; // Item already exists, do nothing or handle duplicates as needed
        } else { // comp > 0
            node.right = insert(node.right, item);
        }

        // DONE: Rotate left operation (handle "middle of three" and "right-leaning red" structures)
        if (isRed(node.left) && isRed(node.left.right)) {
            node.left = rotateRight(node.left); // Rotate right if left child and its left child are red
        } // can then proceed to the "smallest of three" scenario
        if (isRed(node.right) && !isRed(node.left)) {
            node = rotateLeft(node); // Rotate left if right child is red and left child is black
        }

        // DONE: Rotate right operation (handle "smallest of three" structure)
        if (isRed(node.left) && isRed(node.left.left)) {
            node = rotateRight(node); // Rotate right if left child and its left child are red
        }

        // DONE: Color flip (handle "largest of three" structure)
        if (isRed(node.left) && isRed(node.right)) {
            flipColors(node); // Flip colors if both children are red
        }

        return node; // DONE: fix this return statement
    }

    /**
     * Helper method that returns whether the given node is red. Null nodes (children or leaf
     * nodes) are automatically considered black.
     * @param node the node to check
     * @return true if the node is red, false if it is black or null
     */
    private boolean isRed(RBTreeNode<T> node) {
        return node != null && !node.isBlack;
    }

}
