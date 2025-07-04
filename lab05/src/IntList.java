import java.util.Objects;

/** A data structure to represent a Linked List of Integers.
 * Each IntList represents one node in the overall Linked List.
 */

public class IntList {

    /** The integer stored by this node. */
    public int item;
    /** The next node in this IntList. */
    public IntList next;

    /** Constructs an IntList storing ITEM and next node NEXT. */
    public IntList(int item, IntList next) {
        this.item = item;
        this.next = next;
    }

    /** Constructs an IntList storing ITEM and no next node. */
    public IntList(int item) {
        this(item, null);
    }

    /** Returns an IntList consisting of the elements in ITEMS.
     * IntList L = IntList.list(1, 2, 3);
     * System.out.println(L.toString()) // Prints 1 2 3 */
    public static IntList of(int... items) {
        /** Check for cases when we have no element given. */
        if (items.length == 0) {
            return null;
        }
        /** Create the first element. */
        IntList head = new IntList(items[0]);
        IntList last = head;
        /** Create rest of the list. */
        for (int i = 1; i < items.length; i++) {
            last.next = new IntList(items[i]);
            last = last.next;
        }
        return head;
    }

    /**
     * Returns [position]th item in this list. Throws IllegalArgumentException
     * if index out of bounds.
     *
     * @param position, the position of element.
     * @return The element at [position]
     */
    public int get(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        // * recursive version
        if (position == 0) {
            return this.item;
        }
        if (this.next == null) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return this.next.get(position - 1);

        // // * iterative version
        // IntList current = this;
        // for (int i = 0; i < position; i++) {
        //     if (current == null) {
        //         throw new IllegalArgumentException("Index out of bounds");
        //     }
        //     current = current.next;
        // }
        // if (current == null) {
        //     throw new IllegalArgumentException("Index out of bounds");
        // }
        // return current.item;
    }

    /**
     * Returns the string representation of the list. For the list (1, 2, 3),
     * returns "1 2 3".
     *
     * @return The String representation of the list.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        IntList current = this;
        while (current != null) {
            sb.append(current.item);
            if (current.next != null) {
                sb.append(" ");
            }
            current = current.next;
        }
        return sb.toString();
    }

    /**
     * Returns whether this and the given list or object are equal.
     *
     * NOTE: A full implementation of equals requires checking if the
     * object passed in is of the correct type, as the parameter is of
     * type Object. This also requires we convert the Object to an
     * IntList, if that is legal. The operation we use to do this is called
     * casting, and it is done by specifying the desired type in
     * parentheses. An example of this is `IntList otherLst = (IntList) obj;`
     * We recommend reviewing the `instanceOf` keyword mentioned in the spec.
     *
     * @param obj, another list (object)
     * @return Whether the two lists are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntList otherList)) return false;

        IntList a = this;
        IntList b = otherList;

        while (a != null && b != null) {
            if (a.item != b.item) return false;
            a = a.next;
            b = b.next;
        }

        return a == null && b == null;
    }

    @Override // always override hashCode when overriding equals
    public int hashCode() {
        return Objects.hash(item, next);
    }

    /**
     * Adds the given value at the end of the list.
     *
     * @param value, the int to be added.
     */
    public void add(int value) {
        IntList current = this;
        while (current.next != null) {
            current = current.next;
        }
        current.next = new IntList(value);
    }

    /**
     * Returns the smallest element in the list.
     *
     * @return smallest element in the list
     */
    public int smallest() {
        if (this == null) {
            throw new IllegalArgumentException("List is empty");
        }
        int smallest = this.item;
        IntList current = this.next;
        while (current != null) {
            if (current.item < smallest) {
                smallest = current.item;
            }
            current = current.next;
        }
        return smallest;
    }

    /**
     * Returns the sum of squares of all elements in the list.
     *
     * @return The sum of squares of all elements.
     */
    public int squaredSum() {
        int sum = 0;
        IntList current = this;
        while (current != null) {
            sum += current.item * current.item;
            current = current.next;
        }
        return sum;
    }

    /**
     * Destructively squares each item of the list.
     *
     * @param L list to destructively square.
     */
    public static void dSquareList(IntList L) {
        while (L != null) {
            L.item = L.item * L.item;
            L = L.next;
        }
    }

    /**
     * Returns a list equal to L with all elements squared. Non-destructive.
     *
     * @param L list to non-destructively square.
     * @return the squared list.
     */
    public static IntList squareListIterative(IntList L) {
        if (L == null) {
            return null;
        }
        IntList res = new IntList(L.item * L.item, null);
        IntList ptr = res;
        L = L.next;
        while (L != null) {
            ptr.next = new IntList(L.item * L.item, null);
            L = L.next;
            ptr = ptr.next;
        }
        return res;
    }

    /** Returns a list equal to L with all elements squared. Non-destructive.
     *
     * @param L list to non-destructively square.
     * @return the squared list.
     */
    public static IntList squareListRecursive(IntList L) {
        if (L == null) {
            return null;
        }
        return new IntList(L.item * L.item, squareListRecursive(L.next));
    }

    /**
     * Returns a new IntList consisting of A followed by B,
     * non-destructively (no modifications to A).
     *
     * @param A list to be on the front of the new list.
     * @param B list to be on the back of the new list.
     * @return new list with A followed by B.
     */
    public static IntList catenate(IntList A, IntList B) {
        if (A == null) {
            return B; // If A is null, return B
        }
        IntList head = new IntList(A.item); // Create a new head for the result
        IntList current = head; // Pointer to build the new list
        A = A.next; // Move to the next element in A

        // Copy elements from A to the new list
        while (A != null) {
            current.next = new IntList(A.item);
            current = current.next;
            A = A.next;
        }

        // Now append elements from B
        while (B != null) {
            current.next = new IntList(B.item);
            current = current.next;
            B = B.next;
        }

        return head; // Return the new list
    }

    /**
     * Returns a new IntList consisting of A followed by B,
     * destructively (make modifications to A).
     *
     * @param A list to be on the front of the new list.
     * @param B list to be on the back of the new list.
     * @return new list with A followed by B.
     */
    public static IntList dcatenate(IntList A, IntList B) {
        if (A == null) {
            return B; // If A is null, return B
        }
        IntList current = A; // Pointer to traverse A

        // Traverse to the end of A
        while (current.next != null) {
            current = current.next;
        }

        // Now append B to the end of A
        current.next = B;

        return A; // Return the modified list A
    }
}
