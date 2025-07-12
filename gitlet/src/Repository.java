package gitlet;

// DONE: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.nio.file.*;
import java.nio.file.attribute.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 * <p>
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Gravifer
 */
public class Repository {
    /*
     * DONE: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* DONE: fill in the rest of this class. */

    /** The persisted store of serializations. */
    public static final File OBJ_DIR = join(GITLET_DIR, "objects");

    /** The hashes various refs point to. */
    public static final File REF_DIR = join(GITLET_DIR, "refs");

    /** The hashes various branches point to. */
    public static final File BRC_DIR = join(GITLET_DIR, "heads");

    /** The hashes various tags point to. */
    public static final File TAG_DIR = join(GITLET_DIR, "tags");

    /** The HEAD file saves the HEAD pointer UID */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    protected String HEAD;

    /** The index file, aka "current directory cache",
     * contains the serialization of blobs representing staged files. */
    public static final File INDX_FILE = join(GITLET_DIR, "index");
    protected final StagingArea stagingArea = new StagingArea();

    /** The description file saves the description of the repo */
    public static final File DESC_FILE = join(GITLET_DIR, "description");
    protected String description = "Unnamed repository; edit this file 'description' to name the repository.\n";


    /** Initializes the {@code .gitlet} directory, aka the Gitlet database.
     * <p>
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit:
     * a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
     * It will have a single branch: main, which initially points to this initial commit, and main will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
     * in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0).
     * Since the initial commit in all repositories created by Gitlet will have exactly the same content,
     * it follows that all repositories will automatically share this commit (they will all have the same UID)
     * and all commits in all repositories will trace back to it.
     *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
     *
     * @implNote for now, it is not clear if repo initialization should be moved to another class
     */
    protected static void init_db() throws IOException {
        // * the provided Repository class uses CWD as the repo root naively,
        // * so we should always do verifications in Main
        if (Repository.GITLET_DIR.exists()) {
            // * the spec does not ask for reinitialization of the repository, so we should not allow it
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        String dbPath = Repository.GITLET_DIR.getAbsolutePath();
        try {
            if (Repository.GITLET_DIR.mkdir()) {
                // if the directory was created successfully, we can initialize the repository
                // DONE: create the initial commit
                // TODO: create the default branch ("master")
                Commit initialCommit = Commit.initialCommit(); // initial commit is empty and a singleton, so no need to persist it
                // nevertheless, we should initialize the object database
                for (File dir : new File[]{Repository.OBJ_DIR, Repository.REF_DIR, Repository.BRC_DIR, Repository.TAG_DIR}) {
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw error("Unable to create directory: " + dir.getAbsolutePath());
                    }
                    if (!dir.isDirectory()) {
                        throw error("The path " + dir.getAbsolutePath() + " is not a directory.");
                    }
                }

                if (!Repository.HEAD_FILE.createNewFile() && !Repository.HEAD_FILE.createNewFile()) { // create the HEAD file
                    throw error("Unable to create HEAD file in .gitlet/ directory.");
                }
                writeContents(Repository.HEAD_FILE, initialCommit.getUid()); // write the initial commit UID to HEAD
                System.err.println("Initialized an empty Gitlet repository in " + dbPath); // Per spec, no output on System.out
            } else {
                System.err.printf("init_db(): java.io.File.mkdir() returned false for " + dbPath);
                // System.exit(1); // ! Per spec, always exit with exit code 0, even in the presence of errors.
                throw new IOException(String.format("Unable to create directory at " + dbPath));
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while initializing the Gitlet repository: " + e.getMessage());
            throw e; // new GitletException("Unable to create .gitlet/ directory.");
        }
    }

    public void stageFile(String filePath) {
        // * stage a file for commit
        // * this method should add the file to the staging area
        // * if the file is already staged, it should update the staging area
        File file = join(CWD, filePath);
        if (!file.exists()) {
            throw error("File does not exist: " + filePath);
        }
        Blob blob = Blob.blobify(file); // persist the blob to the object database
        // * add the file to the staging area
        // * we should also update the staging area with the file information
        // * the fileInfo object should contain the file path, blob UID, creation time, last modified time, and size
        long mtime = file.lastModified(); // last modified time
        long ctime = mtime;
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            ctime = attrs.creationTime().toMillis(); // creation time
        } catch (IOException e) {
            System.err.println("Can't get creation time of " + file.getAbsolutePath());
        }
        long size = file.length(); // size in bytes
        stagingArea.stagedFiles.put(filePath, new StagingArea.fileInfo(filePath, blob.getUid(), ctime, mtime, size));
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
    }
}
