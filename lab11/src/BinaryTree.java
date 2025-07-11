import java.util.ArrayList;

public class BinaryTree<T> {

    // Do not modify the TreeNode class.
    static class TreeNode<T> {
        T item;
        TreeNode<T> left;
        TreeNode<T> right;

        public TreeNode(T item) {
            this.item = item; left = right = null;
        }

        public TreeNode(T item, TreeNode<T> left, TreeNode<T> right) {
            this.item = item;
            this.left = left;
            this.right = right;
        }

        public T getItem() {
            return item;
        }

        public TreeNode<T> getLeft() {
            return left;
        }

        public TreeNode<T> getRight() {
            return right;
        }
    }

    // protected gives subclasses the ability to access this instance variable,
    // but not other classes.
    TreeNode<T> root;

    public BinaryTree() {
        root = null;
    }

    public BinaryTree(TreeNode<T> t) {
        root = t;
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    /** Optional constructor, see optional exercise in lab (or last week's theoretical lab). */
    public BinaryTree(ArrayList<T> pre, ArrayList<T> in) {
        if (pre == null || in == null || pre.size() != in.size()) {
            throw new IllegalArgumentException("Preorder and inorder lists must be non-null and of the same size.");
        }
        root = buildTree(pre, in, 0, pre.size() - 1, 0, in.size() - 1);
    }

    private TreeNode<T> buildTree(ArrayList<T> pre, ArrayList<T> in, int preStart, int preEnd, int inStart, int inEnd) {
        if (preStart > preEnd || inStart > inEnd) {
            return null; // base case for recursion
        }
        T rootValue = pre.get(preStart);
        TreeNode<T> node = new TreeNode<>(rootValue);

        // Find the index of the root value in the inorder list
        int rootIndexInInorder = in.indexOf(rootValue);
        int leftSize = rootIndexInInorder - inStart;

        // Recursively build the left and right subtrees
        node.left = buildTree(pre, in, preStart + 1, preStart + leftSize, inStart, rootIndexInInorder - 1);
        node.right = buildTree(pre, in, preStart + leftSize + 1, preEnd, rootIndexInInorder + 1, inEnd);

        return node;
    }

    /* Print the values in the tree in preorder. */
    public void printPreorder() {
        if (root == null) {
            System.out.println("(empty tree)");
        } else {
            printPreorderHelper(root);
            System.out.println();
        }
    }

    private void printPreorderHelper(TreeNode<T> node) {
        if (node == null) {
            return;
        }
        System.out.print(node.item + " ");
        printPreorderHelper(node.left);
        printPreorderHelper(node.right);
    }

    /* Print the values in the tree in inorder: values in the left subtree
       first (in inorder), then the root value, then values in the first
       subtree (in inorder). */
    public void printInorder() {
        if (root == null) {
            System.out.println("(empty tree)");
        } else {
            printInorderHelper(root);
            System.out.println();
        }
    }

    /* Prints the nodes of the BinaryTree in inorder. Used for your testing. */
    private void printInorderHelper(TreeNode<T> node) {
        if (node == null) {
            return;
        }
        printInorderHelper(node.left);
        System.out.print(node.item + " ");
        printInorderHelper(node.right);
    }

    /* Prints out the contents of a BinaryTree with a description in both
       preorder and inorder. */
    static void print(BinaryTree t, String description) {
        System.out.println(description + " in preorder");
        t.printPreorder();
        System.out.println(description + " in inorder");
        t.printInorder();
        System.out.println();
    }

    /* Fills this BinaryTree with values a, b, and c. DO NOT MODIFY. */
    public static BinaryTree<String> sampleTree1() {
        TreeNode<String> root = new TreeNode("a",
                new TreeNode("b"),
                new TreeNode("c"));
        return new BinaryTree<>(root);
    }

    /* Fills this BinaryTree with values a, b, and c, d, e, f. DO NOT MODIFY. */
    public static BinaryTree<String> sampleTree2() {
        TreeNode root = new TreeNode("a",
                new TreeNode("b",
                        new TreeNode("d",
                                new TreeNode("e"),
                                new TreeNode("f")),
                        null),
                new TreeNode("c"));
        return new BinaryTree<>(root);
    }

    /* Fills this BinaryTree with the values a, b, c, d, e, f. DO NOT MODIFY. */
    public static BinaryTree<String> sampleTree3() {
        TreeNode<String> root = new TreeNode("a",
                new TreeNode("b"),
                new TreeNode("c",
                        new TreeNode("d",
                                new TreeNode("e"),
                                new TreeNode("f")),
                        null));
        return new BinaryTree<>(root);
    }

    /* Fills this BinaryTree with the same leaf TreeNode. DO NOT MODIFY. */
    public static BinaryTree<String> sampleTree4() {
        TreeNode<String> leafNode = new TreeNode("c");
        TreeNode<String> root = new TreeNode("a", new TreeNode("b", leafNode, leafNode),
                new TreeNode("d", leafNode, leafNode));
        return new BinaryTree<>(root);
    }

    /* Creates two BinaryTrees and prints them out in inorder. */
    public static void main(String[] args) {
        BinaryTree<String> t = new BinaryTree<>();
        print(t, "the empty tree");
        t = BinaryTree.sampleTree1();
        print(t, "sample tree 1");
        t = BinaryTree.sampleTree2();
        print(t, "sample tree 2");
        t = BinaryTree.sampleTree3();
        print(t, "sample tree 3");
        t = BinaryTree.sampleTree4();
        print(t, "sample tree 4");
    }

    /* Returns the height of the tree. */
    public int height() {
        // DONE: YOUR CODE HERE
        return heightHelper(root);
    }

    private int heightHelper(TreeNode<T> root) {
        if (root == null) {
            return 0; // the height of an empty tree is 0 per spec; -1 would be more monadic
        }
        int leftHeight = heightHelper(root.left);
        int rightHeight = heightHelper(root.right);
        return Math.max(leftHeight, rightHeight) + 1; // add 1 for the current node
    }

    /* Returns true if the tree's left and right children are the same height,
       and are themselves completely balanced. */
    public boolean isCompletelyBalanced() {
        // DONE: YOUR CODE HERE
        return isCompletelyBalancedHelper(root);
    }

    private boolean isCompletelyBalancedHelper(TreeNode<T> root) {
        if (root == null) {
            return true; // an empty tree is balanced
        }
        int leftHeight = heightHelper(root.left);
        int rightHeight = heightHelper(root.right);
        if (leftHeight != rightHeight) {
            return false; // heights are not equal
        }
        // check if both subtrees are completely balanced
        return isCompletelyBalancedHelper(root.left) && isCompletelyBalancedHelper(root.right);
    }

    /* Returns a BinaryTree representing the Fibonacci calculation for N. */
    public static BinaryTree<Integer> fibTree(int N) {
        BinaryTree<Integer> result = new BinaryTree<Integer>();
        if (N < 0) {
            throw new IllegalArgumentException("N must be non-negative");
        }
        if (N == 0) {
            result.root = new TreeNode<>(0);
        } else if (N == 1) {
            result.root = new TreeNode<>(1);
        } else {
            TreeNode<Integer> left = fibTree(N - 1).getRoot();
            TreeNode<Integer> right = fibTree(N - 2).getRoot();
            result.root = new TreeNode<>(left.item + right.item, left, right);
        }
        return result;
    }
}
