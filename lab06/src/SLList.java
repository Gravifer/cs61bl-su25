/**
 * An SLList is a list of integers, which encapsulates the
 * naked linked list structure.
 */
public class SLList {

    /* The first item (if it exists) is at sentinel.next. */
    private IntListNode sentinel;
    private int size;
    public static final int SENTINEL_VAL = 42;

    /**
     * IntListNode is a nested class that represents a single node in the
     * SLList, storing an item and a reference to the next IntListNode.
     */
    private static class IntListNode {
        /*
         * The access modifiers inside a private nested class are irrelevant:
         * both the inner class and the outer class can access these instance
         * variables and methods.
         */
        private int item;
        private IntListNode next;

        public IntListNode(int item, IntListNode next) {
            this.item = item;
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IntListNode other)) {
                return false;
            }
            return item == other.item;
        }

        @Override
        public String toString() {
            return item + "";
        }

    }

    /** Creates an empty SLList. */
    public SLList() {
        sentinel = new IntListNode(SENTINEL_VAL, null);
        sentinel.next = sentinel;
        size = 0;
    }

    public SLList(int x) {
        sentinel = new IntListNode(SENTINEL_VAL, null);
        sentinel.next = new IntListNode(x, null);
        sentinel.next.next = sentinel;
        size = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SLList other)) {
            return false;
        }
        if (size != other.size) {
            return false;
        }

        IntListNode l1 = sentinel.next;
        IntListNode l2 = other.sentinel.next;

        // because we checked size earlier, l2 will also reach sentinel when l1 reaches sentinel
        while (l1 != sentinel) {
            if (!l1.equals(l2)) {
                return false;
            }
            l1 = l1.next;
            l2 = l2.next;
        }
        return l1.equals(sentinel) && l2.equals(other.sentinel);
    }

    @Override
    public String toString() {
        IntListNode l = sentinel.next;
        String result = "";

        while (l != sentinel) {
            result += l + " ";
            l = l.next;
        }

        return result.trim();
    }

    /** Returns an SLList consisting of the given values. */
    public static SLList of(int... values) {
        SLList list = new SLList();
        for (int i = values.length - 1; i >= 0; i -= 1) {
            list.addFirst(values[i]);
        }
        return list;
    }

    /** Returns the size of the list. */
    public int size() {
        return size;
    }

    /** Adds x to the front of the list. */
    public void addFirst(int x) {
        sentinel.next = new IntListNode(x, sentinel.next);
        size += 1;
    }

    /** Adds x to the list at the specified index.
     * When index is out of bound, simply append at the end of the list. */
    public void add(int index, int x) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
        } else if (index > size) { // * per spec
            index = size; // Adjust index to append at the end
        }
        IntListNode current = sentinel;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        current.next = new IntListNode(x, current.next);
        size += 1;
    }

    /** Destructively reverses this list. */
    public void reverse() {
        IntListNode prev = sentinel;
        IntListNode current = sentinel.next;
        IntListNode next;

        while (current != sentinel) {
            next = current.next; // Save the next node
            current.next = prev; // Reverse the link
            prev = current; // Move prev to current
            current = next; // Move to the next node
        }

        // Update the sentinel's next to point to the new first element
        sentinel.next = prev;
    }
}
