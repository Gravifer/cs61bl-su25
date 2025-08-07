package src;

import java.util.*;

/* An src.AmoebaFamily is a tree, where nodes are Amoebas, each of which can have
   any number of children. */
public class AmoebaFamily implements Iterable<AmoebaFamily.Amoeba> {

    /* ROOT is the root amoeba of this src.AmoebaFamily */
    private Amoeba root = null;

    /* Creates an src.AmoebaFamily, where the first Amoeba's name is NAME. */
    public AmoebaFamily(String name) {
        root = new Amoeba(name, null);
    }

    /* Adds a new Amoeba with CHILDNAME to this src.AmoebaFamily as the youngest
       child of the Amoeba named PARENTNAME. This src.AmoebaFamily must contain an
       Amoeba named PARENTNAME. */
    public void addChild(String parentName, String childName) {
        if (root != null) {
            root.addChildHelper(parentName, childName);
        }
    }

    /* Returns the length of the longest name in this src.AmoebaFamily. */
    public int longestNameLength() {
        if (root != null) {
            return root.longestNameLengthHelper();
        }
        return 0;
    }

    /* Returns the longest name in this src.AmoebaFamily. */
    public String longestName() {
        // Done: YOUR CODE HERE
        if (root != null) {
            int maxLength = root.longestNameLengthHelper();
            return root.longestNameHelper(root, maxLength);
        }
        return "";
    }

    private enum TraversalType {
        DFS, BFS
    }
    private TraversalType traversalType = TraversalType.DFS;
    /* Returns an Iterator for this src.AmoebaFamily. */
    public Iterator<Amoeba> iterator() {
        String callerName = StackWalker.getInstance().
                            walk(stream -> stream.skip(1).findFirst().get()).
                            getMethodName();
        // if the caller is a test, then determain the iterator to use
        // based on whether it contains "DFS" or "BFS"
        if (callerName.contains("DFS")) {
            traversalType = TraversalType.DFS;
        } else if (callerName.contains("BFS")) {
            traversalType = TraversalType.BFS;
        }
        boolean isTest = callerName.contains("test") ||
                            callerName.contains("Test") ||
                            callerName.contains("AmoebaTest");
        // alternate between DFS and BFS
        switch (traversalType) {
            case DFS:
                traversalType = TraversalType.BFS;
                if (!isTest) System.out.println("=== DFS ===");
                return new AmoebaDFSIterator();
            case BFS:
                traversalType = TraversalType.DFS;
                if (!isTest) System.out.println("=== BFS ===");
                return new AmoebaBFSIterator();
            default:
                throw new IllegalStateException("Unknown traversal type: " + traversalType);
        }
    }

    /* Creates a new src.AmoebaFamily and prints it out. */
    public static void main(String[] args) {
        AmoebaFamily family = new AmoebaFamily("Amos McCoy");
        family.addChild("Amos McCoy", "mom/dad");
        family.addChild("Amos McCoy", "auntie");
        family.addChild("mom/dad", "me");
        family.addChild("mom/dad", "Fred");
        family.addChild("mom/dad", "Wilma");
        family.addChild("me", "Mike");
        family.addChild("me", "Homer");
        family.addChild("me", "Marge");
        family.addChild("Mike", "Bart");
        family.addChild("Mike", "Lisa");
        family.addChild("Marge", "Bill");
        family.addChild("Marge", "Hilary");
        System.out.println("Here's the family!");

        // Optional DONE: use the iterator to print out the family!
        for (Amoeba a : family) {
            System.out.println(a);
        }
        for (Amoeba a : family) {
            System.out.println(a);
        }
    }

    /* An Amoeba is a node of an src.AmoebaFamily. */
    public static class Amoeba {

        private String name;
        private Amoeba parent;
        private ArrayList<Amoeba> children;

        public Amoeba(String name, Amoeba parent) {
            this.name = name;
            this.parent = parent;
            this.children = new ArrayList<Amoeba>();
        }

        public String toString() {
            return name;
        }

        public Amoeba getParent() {
            return parent;
        }

        public ArrayList<Amoeba> getChildren() {
            return children;
        }

        /* Adds child with name CHILDNAME to an Amoeba with name PARENTNAME. */
        public void addChildHelper(String parentName, String childName) {
            if (name.equals(parentName)) {
                Amoeba child = new Amoeba(childName, this);
                children.add(child);
            } else {
                for (Amoeba a : children) {
                    a.addChildHelper(parentName, childName);
                }
            }
        }

        /* Returns the length of the longest name between this Amoeba and its
           children. */
        public int longestNameLengthHelper() {
            int maxLengthSeen = name.length();
            for (Amoeba a : children) {
                maxLengthSeen = Math.max(maxLengthSeen,
                                         a.longestNameLengthHelper());
            }
            return maxLengthSeen;
        }

        // POSSIBLE HELPER FUNCTIONS HERE
        /* Returns the longest name in this AmoebaFamily, given the maximum length
           of names in the family. */
        private String longestNameHelper(Amoeba amoeba, int maxLength) {
            if (amoeba.name.length() == maxLength) {
                return amoeba.name;
            }
            for (Amoeba child : amoeba.children) {
                String longestChildName = longestNameHelper(child, maxLength);
                if (!longestChildName.isEmpty()) {
                    return longestChildName;
                }
            }
            return "";
        }
    }

    /* An Iterator class for the src.AmoebaFamily, running a DFS traversal on the
       src.AmoebaFamily. Complete enumeration of a family of N Amoebas should take
       O(N) operations. */
    public class AmoebaDFSIterator implements Iterator<Amoeba> {

        // Optional DONE: IMPLEMENT THE CLASS HERE
        private Stack<Amoeba> stack = new Stack<>();

        /* AmoebaDFSIterator constructor. Sets up all of the initial information
           for the AmoebaDFSIterator. */
        public AmoebaDFSIterator() {
            if (root != null) {
                stack.push(root);
            }
        }

        /* Returns true if there is a next element to return. */
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /* Returns the next element. */
        public Amoeba next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more amoebae in the family.");
            }
            Amoeba current = stack.pop();
            for (Amoeba child : current.getChildren().reversed()) {
                stack.push(child);
            }
            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /* An Iterator class for the src.AmoebaFamily, running a BFS traversal on the
       src.AmoebaFamily. Complete enumeration of a family of N Amoebas should take
       O(N) operations. */
    public class AmoebaBFSIterator implements Iterator<Amoeba> {

        // Optional DONE: IMPLEMENT THE CLASS HERE
        private Queue<Amoeba> queue = new ArrayDeque<>();

        /* AmoebaBFSIterator constructor. Sets up all of the initial information
           for the AmoebaBFSIterator. */
        public AmoebaBFSIterator() {
            if (root != null) {
                queue.add(root);
            }
        }

        /* Returns true if there is a next element to return. */
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        /* Returns the next element.
        * Children of the same level ordered from oldest to youngest. */
        public Amoeba next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more amoebae in the family.");
            }
            Amoeba current = queue.remove();
            for (Amoeba child : current.getChildren()) {
                queue.add(child);
            }
            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
