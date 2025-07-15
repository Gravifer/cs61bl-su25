package gitlet;


// DONE: any imports you need here
import java.io.File;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.Instant; // * no need for this, Date is sufficient

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 *
 *  <blockquote>DONE: It's a good idea to give a description here of what else this Class
 *  does at a high level.</blockquote>
 *  <p>
 *  This class encapsulates the properties and behaviors of a commit in the gitlet version control system.
 *  It includes the commit message, timestamp, parent commits, and the files associated with the commit.
 *  Commits are immutable and uniquely identified by their UID, which is generated based on their attributes.
 *  Commits can be created with a message, parent commit, and a list of files to be included in the commit.
 *  The class provides methods to access parent commits, retrieve file blobs, and compare commits.
 *  It also implements the Comparable interface to allow sorting of commits based on their relationships
 *  (ancestor, descendant, or co-linear).
 *  <p>
 *
 * @implNote This map is always initialized in the constructor for new objects,
 * but since it is transient, it will be null after deserialization. Therefore,
 * all accessors (such as getParentCommit) will lazily rebuild it if needed.
 * This ensures correctness and performance for both new and deserialized objects.
 *
 * @author Gravifer
 */
public class Commit implements Serializable, Comparable<Commit>, Dumpable {
    /* DONE: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    // * Commit instances should be immutable, so attributes are marked final. No need to make them private.

    // /** The unique hash ID of this Commit. */
    // protected final String uid; // ! it's moot to record ones uid in itself
    // /** Returns the unique ID of this Commit.
    //  *  <p>
    //  *  The UID is generated based on the commit message, timestamp, and parent commits.
    //  *  It is used to uniquely identify this commit in the repository.
    //  */
    // private String mkUid() { // sha1 only accepts String or byte[] as input, so we need to convert the commit attributes to a String.
    //     // Generate a unique ID for this commit based on its message, timestamp, authorTimestamp, and parents.
    //
    //     return sha1("commit ", serialize(message), serialize(timestamp), serialize(authorTimestamp), serialize(parents));
    // }

    /**
     * The parent Commits of this Commit; should only have one parent for non-merge Commits.
     * <p>
     * For merge commits, this array can have more than one parent.
     */
    protected final String[] parents;

    /**
     * Lazily loaded transient map for parent commits.
     * Not serialized; rebuilt on demand after deserialization.
     *
     * <p>
     * @implNote This map is always initialized in the constructor for new objects,
     * but since it is transient, it will be null after deserialization. Therefore,
     * all accessors (such as getParentCommit) will lazily rebuild it if needed.
     * This ensures correctness and performance for both new and deserialized objects.
     *
     * <p>
     * The parent commit objects are cached in this map, keyed by their UID.
     * This avoids repeated deserialization and lookup for parent commits.
     */
    transient Map<String, Commit> parentsMap;

    /**
     * Lazily initialize parentsMap if needed.
     * Called by all parent commit accessors to ensure the cache is available.
     * This is necessary because transient fields are not restored after deserialization.
     */
    private void initParentsMap() { // transient, need to rebuild on demand after deserialization, although forced at initialization
        if (parentsMap == null) {
            parentsMap = new HashMap<>();
            for (String parent : parents) {
                parentsMap.put(parent, Commit.getByUid(parent));  // getByUid guarantees to return the identical initial commit.
            }
        }
    }

    /**
     * Get the parent commit by index, lazily loading parentsMap if needed.
     */
    public Commit getParentCommit(int idx) {
        initParentsMap();
        if (idx < 0 || idx >= parents.length) return null;
        return parentsMap.get(parents[idx]);
    }
    /**
     * Get the first parent commit (default).
     */
    public Commit getParentCommit() {
        return getParentCommit(0);
    }

    /**
     * The timestamp of this Commit.
     */
    protected final Instant timestamp;
    protected final Instant authorTimestamp;

    /**
     * The message of this Commit.
     */
    protected final String message;

    protected final boolean isEmpty;

    /**
     * The blobs (file contents) associated with this Commit.
     * <p>
     * This is a map from file names to their corresponding Blob objects.
     * It represents the state of the files in the repository at the time of this commit.
     */
    private final Map<String, String> fileBlobs;

    /* DONE: fill in the rest of this class. */

    /**
     * Creates a new Commit with the given message.
     *
     * @param message    the commit message
     * @param parent     the UID of the parent commit
     * @param files      the list of files to be included in this commit
     * @param allowEmpty whether to allow an empty commit (no files)
     */
    public Commit(String message, String parent, List<File> files, boolean allowEmpty) {
        // somehow, before v22, Java doesn't allow calling a this(...) ctor after preparing arguments, so I'm repeating myself here
        this.isEmpty = files == null || files.isEmpty();
        if (!allowEmpty && isEmpty) {
            throw error("Nothing to commit.");
        }
        this.parents = new String[]{parent};
        initParentsMap();
        Commit parentCommit = getParentCommit();  // Commit.getByUid() checks if the parent commit is initial, so no need to check here
        this.timestamp = Instant.now(); // use current time
        this.authorTimestamp = timestamp;
        this.message = message;
        // this.fileBlobs = parentCommit.fileBlobs == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parentCommit.fileBlobs);
        this.fileBlobs = new LinkedHashMap<>();
        // Always use putAll for clarity, even if parentCommit.fileBlobs is empty
        if (parentCommit != null && parentCommit.fileBlobs != null) {
            this.fileBlobs.putAll(parentCommit.fileBlobs);
        }
        // * initialize fileBlobs with the contents of the files
        // * we skipped the process of staging the files first, so that selective commiting becomes possible
        if (files != null) {
            files.sort(Comparator.comparing(File::getPath));
            for (File file : files) {
                if (file.exists()) {
                    Blob blob = Blob.blobify(file); // persist the blob to the object database
                    fileBlobs.put(file.getPath(), blob.getUid());
                } else {
                    throw error("File does not exist: " + file.getAbsolutePath());
                }
            }
        }
        // * persist the commit itself
        this.persist();
    }
    public Commit(String message, String parent, List<File> files){
        this(message, parent, files, false);
    }

    /**
     * Creates a new Commit with the given message.
     *
     * @param message           the commit message
     * @param parents           the UIDs of the parent commits
     * @param fileBlobsToAdd    the map of staged files (file paths to their Blob UIDs)
     * @param fileBlobsToRemove the map of removed files (file paths to their Blob UIDs); can be omitted
     * @param allowEmpty        whether to allow an empty commit (no files); default to false if omitted
     */
    public Commit(String message, String[] parents, Map<String, String> fileBlobsToAdd, Map<String, String> fileBlobsToRemove, boolean allowEmpty) {
        this.isEmpty = fileBlobsToAdd == null || fileBlobsToAdd.isEmpty();
        if (!allowEmpty && isEmpty) {
            throw error("Nothing to commit.");
        }
        this.parents = parents; // new String[]{parent};
        initParentsMap();
        Commit parentCommit = getParentCommit(); // Commit.getByUid() checks if the parent commit is initial, so no need to check here
        this.timestamp = Instant.now(); // use current time
        this.authorTimestamp = timestamp;
        this.message = message;
        this.fileBlobs = new LinkedHashMap<>();
        // Always use putAll for clarity, even if parentCommit.fileBlobs is empty
        if (parentCommit != null && parentCommit.fileBlobs != null) {
            this.fileBlobs.putAll(parentCommit.fileBlobs);
        }
        // * merge the fileBlobsToAdd with the parent commit's fileBlobs
        if (fileBlobsToAdd != null) {
            this.fileBlobs.putAll(fileBlobsToAdd);
        }
        // * remove the fileBlobsToRemove from the parent commit's fileBlobs
        if (fileBlobsToRemove != null) {
            for (String filePath : fileBlobsToRemove.keySet()) {
                this.fileBlobs.remove(filePath);
            }
        }
        // * persist the commit itself
        this.persist();
    }
    public Commit(String message, String parent, Map<String, String> fileBlobsToAdd, Map<String, String> fileBlobsToRemove, boolean allowEmpty) {
        this(message, new String[]{parent}, fileBlobsToAdd, fileBlobsToRemove, allowEmpty);
    }
    public Commit(String message, String parent, Map<String, String> fileBlobsToAdd, boolean allowEmpty) {
        this(message, parent, fileBlobsToAdd, null, allowEmpty);
    }
    public Commit(String message, String parent, Map<String, String> fileBlobs){
        this(message, parent, fileBlobs, false);
    }

    // /**
    //  * The initial commit, which is a singleton.
    //  * <p>
    //  * This is a virtual commit that never gets serialized.
    //  */
    // public static final Commit initialCommit = new Commit(); // * disallowed by the auto-grader
    public static Commit initialCommit() {
        // return initialCommit;
        return new Commit();
    }

    /**
     * Private constructor for <i>the</i> initial (aka root) commit.
     * <p>
     * The initial commit has no parents, a message of "initial commit",
     * and a timestamp of 00:00:00 UTC, Thursday, 1 January 1970.
     * It is created only once and is used as the root of all commit trees.
     */
    private Commit() {
        // if (initialCommit != null) {
        //     throw new IllegalStateException("Initial commit already created.");
        // }
        this.message = "initial commit";
        this.timestamp = Instant.EPOCH; // Thu Jan 1 00:00:00 1970 +0000
        this.authorTimestamp = timestamp; // same as timestamp for initial commit
        this.parents = new String[]{}; // initial commit has no parents (only it should do this)
        this.fileBlobs = new LinkedHashMap<>(); // initial commit has no files
        this.isEmpty = true; // initial commit is empty
    }

    /**
     * Checks if this Commit is the initial (aka root) commit.
     * <p>
     * The initial commit is a special commit that has no parents and a specific UID.
     *
     * @return true if this Commit is the initial commit, false otherwise
     */
    public boolean isInitialCommit() {
        // * the initial commit is a singleton, so we can check if it is the same instance
        return this.getUid().equals(initialCommit().getUid()) && this.parents.length == 0;
    }

    public Map<String, String> getFileBlobs() {
        return this.fileBlobs;
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
     * {@code (x.compareTo(y)==0)} implies only that the commits are not ancestral or descendant of each other.     *
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
        if (this.equals(o)) {
            return 0; // same commit
        }
        return 0;
    } // TODO: implement the actual comparison logic

    /**
     * Only compared by UID. Does <i>not</i> require the other object to be a commit.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Dumpable other)) return false;
        return this.getUid().equals(other.getUid());
        // if (!(o instanceof Commit commit)) return false;
        // return uid.equals(commit.uid);
    }

    /**
     * Returns the hash code of this Commit, which is based on its UID.
     * This is used for storing commits in hash-based collections like HashMap.
     *
     * @return the hash code of this Commit
     */
    @Override
    public int hashCode() {
        return getUid().hashCode();
    }

    @Override
    public String toString() {
        return "Commit{" +
                "uid='" + getUid() + '\'' +
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
    public String getDumpType() {
        return "commit";
    }

    public static Commit getByUid(String uid) {
        // // * get the commit from the object database
        // File file = Dumpable.persistFile(uid);
        // if (!file.exists()) {
        //     throw error("Object does not exist: " + uid);
        // }
        // return readObject(file, Commit.class);
        if (uid.equals(initialCommit().getUid())) {
            return initialCommit(); // return the singleton initial commit
        }
        return Dumpable.getByUid(uid, Commit.class);
    }

    @Override
    public String getUid() {
        // use only core fields to calculate UID, ensure consistency in persistence and deserialization
        StringBuilder sb = new StringBuilder();
        sb.append(getDumpType()).append("\0");
        sb.append(message).append("\0");
        sb.append(timestamp.toString()).append("\0");
        sb.append(authorTimestamp.toString()).append("\0");
        for (String parent : parents) {
            sb.append(parent).append("\0");
        }
        // * sort the fileBlobs by key to ensure consistent order
        fileBlobs.keySet().stream().sorted().forEach(key -> sb.append(key).append(":").append(fileBlobs.get(key)).append("\0"));
        return sha1(sb.toString());
    }

    public void logCommit(DateTimeFormatter formatter) {
        System.out.println("===");
        System.out.println("commit " + getUid());
        // System.out.println("Date: " + currentCommit.timestamp);
        String formattedDate = formatter.format(timestamp); // Format the timestamp using Instant
        System.out.println("Date: " + formattedDate);
        System.out.println(message);
        System.out.println();

        // System.err.println("===");
        // System.err.println("Commit UID: " + getUid());
        // System.err.println("Message: " + message);
        // System.err.println("Timestamp: " + timestamp);
        // System.err.println("Author Timestamp: " + authorTimestamp);
        // System.err.println("Parents: " + String.join(", ", parents));
        // if (fileBlobs.isEmpty()) {
        //     System.err.println("No files in this commit.");
        // } else {
        //     System.err.println("Files in this commit:");
        //     for (Map.Entry<String, String> entry : fileBlobs.entrySet()) {
        //         System.err.printf("  %s -> %s%n", entry.getKey(), entry.getValue());
        //     }
        // }
        // System.err.println();
        System.err.println(message + " (" + getUid() + ")");
    }
    public void logCommit(){
        // Use a default formatter if none is provided
        DateTimeFormatter formatter = DateTimeFormatter // Format timestamp as local time with zone
                .ofPattern("EEE MMM d HH:mm:ss yyyy XX", Locale.US).withZone(ZoneId.systemDefault()); // Local, according to Berkeley
        logCommit(formatter);
    }

    public static long getTime(Object o) {
        return switch (o) {
            case Commit commit -> commit.timestamp.toEpochMilli();
            case Instant instant -> instant.toEpochMilli();
            case Date date -> date.getTime();
            default -> throw new IllegalArgumentException("Unsupported type for getTime: " + o.getClass().getName());
        };
    }

    /**
     * Finds the latest common ancestor (split point) of two commits.
     * This is used for merge operations to determine the base commit.
     * @param c1 The first commit (usually HEAD)
     * @param c2 The second commit (usually the branch to merge)
     * @return The split point commit, or null if none found
     */
    public static Commit findSplitPoint(Commit c1, Commit c2) {
        // Collect all ancestors of c1
        Set<String> ancestors1 = new HashSet<>();
        Deque<Commit> stack1 = new ArrayDeque<>();
        stack1.push(c1);
        while (!stack1.isEmpty()) {
            Commit current = stack1.pop();
            if (current == null || ancestors1.contains(current.getUid())) continue;
            ancestors1.add(current.getUid());
            if (current.parents != null) {
                for (String parentUid : current.parents) {
                    stack1.push(Commit.getByUid(parentUid));
                }
            }
        }
        // Traverse ancestors of c2, return first found in ancestors1
        Deque<Commit> stack2 = new ArrayDeque<>();
        Set<String> visited2 = new HashSet<>();
        stack2.push(c2);
        while (!stack2.isEmpty()) {
            Commit current = stack2.pop();
            if (current == null || visited2.contains(current.getUid())) continue;
            if (ancestors1.contains(current.getUid())) {
                return current;
            }
            visited2.add(current.getUid());
            if (current.parents != null) {
                for (String parentUid : current.parents) {
                    stack2.push(Commit.getByUid(parentUid));
                }
            }
        }
        return null;
    }
}
