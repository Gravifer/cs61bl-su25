package gitlet;


// DONE: any imports you need here
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date; // DONE: You'll likely use this in this class
import java.util.TreeMap;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Commit implements Serializable, Comparable<Commit>, Dumpable {
    /* DONE: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    // * Commit instances should be immutable, so attributes are marked final. No need to make them private.

    /** The unique hash ID of this Commit. */
    protected final String uid;
    /** Returns the unique ID of this Commit.
     *  <p>
     *  The UID is generated based on the commit message, timestamp, and parent commits.
     *  It is used to uniquely identify this commit in the repository.
     */
    private String mkUid() { // sha1 only accepts String or byte[] as input, so we need to convert the commit attributes to a String.
        // Generate a unique ID for this commit based on its message, timestamp, authorTimestamp, and parents.
        return sha1("commit ",message, timestamp.toString(), authorTimestamp.toString(), Arrays.toString(parents));
        // return Utils.sha1((Object) Utils.serialize(this));
    }
    @Override
    public String getUid() {
        return uid;
    }

    /** The parent Commits of this Commit; should only have one parent for non-merge Commits.
     *  <p>
     *  For merge commits, this array can have more than one parent.
     */
    protected final String[] parents;

    /** The timestamp of this Commit. */
    protected final Date timestamp;
    protected final Date authorTimestamp;

    /** The message of this Commit. */
    protected final String message;

    /* DONE: fill in the rest of this class. */

    /**
     * Creates a new Commit with the given message.
     *
     * @param message the commit message
     */
    public Commit(String message, String parent) {
        this.parents = new String[]{parent};
        this.timestamp = new Date(); // use current time
        this.authorTimestamp = timestamp;
        this.message = message;
        this.uid = mkUid();
    }

    // /**
    //  * The initial commit, which is a singleton.
    //  * <p>
    //  * This is a virtual commit that never gets serialized.
    //  */
    // public static final Commit initialCommit = new Commit(); // * disallowed by the auto-grader
    public static Commit initialCommit() {
        // * the initial commit is a singleton, so we return the same instance every time
        return new Commit();
    }
    /** Private constructor for initial commit.
     *  <p>
     *  The initial commit has no parents, a message of "initial commit",
     *  and a timestamp of 00:00:00 UTC, Thursday, 1 January 1970.
     *  It is created only once and is used as the root of all commit trees.
     */
    private Commit() {
        // if (initialCommit != null) {
        //     throw new IllegalStateException("Initial commit already created.");
        // }
        this.message = "initial commit";
        this.timestamp = new Date(0); // Thu Jan 1 00:00:00 1970 +0000
        this.authorTimestamp = timestamp; // same as timestamp for initial commit
        this.parents = new String[]{}; // initial commit has no parents (only it should do this)
        this.uid = mkUid();
    }

    /**
     * Compares this Commit with the specified object (required to be a Commit) for order.
     * Returns a negative integer, zero, or a positive integer
     * as this Commit is ancestor of, not co-linear, or descendant of the specified Commit.
     *
     * <p>It is ensured that {@link Integer#signum signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))}
     * for all {@code x} and {@code y}.  {@code x.compareTo(y)} throws an exception
     * if and only if {@code y.compareTo(x)} throws an exception.
     *
     * <p>The relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>It is ensured that {@code x.compareTo(y)==0} implies that
     * {@code signum(x.compareTo(z)) == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared. Required to be a Commit, otherwise the class cast fails.
     * @return a negative integer, zero, or a positive integer as this Commit
     * is ancestor of, not co-linear, or descendant of the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object. Happens if it is not a Commit.
     * @implNote this class has a topological ordering that is inconsistent with equals.
     *   {@code (x.compareTo(y)==0)} implies only that the commits are not ancestral or descendant of each other.     *
     * @see java.lang.Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Commit o) {
        if (this == o) {
            return 0; // same commit
        }
        if (o == null) {
            throw new NullPointerException("Cannot compare to null.");
        }
        // traverse the graph of commits to determine the relationship
        if (this.uid.equals(o.uid)) {
            return 0; // same commit
        }
        return 0;
    } // TODO: implement the actual comparison logic

    /** Only compared by UID. Does <i>not</i> require the other object to be a commit. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Dumpable other)) return false;
        return uid.equals(other.getUid());
        // if (!(o instanceof Commit commit)) return false;
        // return uid.equals(commit.uid);
    }

    /** Returns the hash code of this Commit, which is based on its UID.
     * This is used for storing commits in hash-based collections like HashMap.
     * @return the hash code of this Commit
     */
    @Override
    public int hashCode() {
        return uid.hashCode();
    }
    @Override
    public String toString() {
        return "Commit{" +
                "uid='" + uid + '\'' +
                ", parents=" + String.join(", ", parents) +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }

    /**
     * Print useful information about this object on System.out.
     */
    @Override
    public void dump() {
        System.out.print(this);
    }

    @Override
    public String getType() {
        return "commit";
    }

    @Override
    public boolean verifyUid() {
        // Verify that the UID is correctly generated based on the commit's attributes.
        return uid.equals(mkUid());
    }
}
