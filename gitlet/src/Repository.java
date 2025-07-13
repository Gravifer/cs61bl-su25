package gitlet;

// DONE: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZonedDateTime;
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
    public String getHead() {
        return HEAD;
    }

    /** The index file, aka "current directory cache",
     * contains the serialization of blobs representing staged files. */
    public static final File INDX_FILE = join(GITLET_DIR, "index");
    /** The staging area, which is a map of file paths to their corresponding Blob UIDs.
     *  This is used to track files that are staged for commit.
     *  <p>
     *  Keys are file paths relative to the repository root,
     *  and values are fileInfo objects, containing some metadata and
     *  the SHA-1 hashes of the Blob objects representing the contents of those files.
     */
    protected final StagingArea stagingArea;

    /** The description file saves the description of the repo */
    public static final File DESC_FILE = join(GITLET_DIR, "description");
    protected String description = "Unnamed repository; edit this file 'description' to name the repository.\n";

    public Repository() {
        // * constructor initializes the repository
        // * it should read the HEAD file and restore the repository to that state
        // * it should also load the staging area from the index file
        // * and load the description from the description file
        this.HEAD = "";
        this.stagingArea = new StagingArea();
    }
    protected Repository(StagingArea stagingArea) {
        // * constructor initializes the repository with a given staging area
        // * this is used for testing purposes
        this.HEAD = "";
        this.stagingArea = stagingArea;
    }


    /** Initializes the {@code .gitlet} directory, aka the Gitlet database.
     *  <p>
     *  Creates a new Gitlet version-control system in the current directory.
     *  This system will automatically start with one commit:
     *  a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
     *  It will have a single branch: main, which initially points to this initial commit, and main will be the current branch.
     *  The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
     *  in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0).
     *  Since the initial commit in all repositories created by Gitlet will have exactly the same content,
     *  it follows that all repositories will automatically share this commit (they will all have the same UID)
     *  and all commits in all repositories will trace back to it.
     *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
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

    /** Reinstantiates the repository from the current HEAD.
     *  <p>
     *  This method is used to restore the repository to a previous state.
     *  It reads the HEAD file and restores the repository to that state.
     *
     *  @return a new Repository instance with the restored state
     */
    public static Repository reinstantiate() {
        // * reinstantiate the repository from the current HEAD
        // * this method should read the HEAD file and restore the repository to that state
        Repository repo;
        // * load the staging area from the index file
        if (INDX_FILE.exists()) {
            repo = new Repository(readObject(INDX_FILE, StagingArea.class));
        } else {
            repo = new Repository();
        }
        if (!HEAD_FILE.exists()) {
            System.err.println("The HEAD file does not exist.");
            throw error("fatal: not a git repository (or any of the parent directories): .gitlet"); // mimic the behavior of git
        }
        repo.HEAD = readContentsAsString(HEAD_FILE);
        // * load the description from the description file
        if (DESC_FILE.exists()) {
            repo.description = readContentsAsString(DESC_FILE);
        }
        return repo;
    }

    /** Stages a file for commit.
     *  <p>
     *  This method adds the file to the staging area. If the file is already staged,
     *  it updates the staging area with the new information.
     *
     *  @param filePath the path of the file to stage
     *
     *  @implSpec Adds a copy of the file as it currently exists to the <i>staging area</i>
     *  (see the description of the {@link Repository#commit} command).
     *  For this reason, adding a file is also called staging the file for addition.
     *  Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     *  The staging area should be somewhere in {@code .gitlet}.
     *  If the current working version of the file is identical to the version in the current commit,
     *  do not stage it to be added, and remove it from the staging area if it is already there
     *  (as can happen when a file is changed, added, and then changed back to its original version).
     *  The file will no longer be staged for removal (see {@code gitlet rm}) if it was at the time of the command.
     *  If the file does not exist, print the error message {@code File does not exist.} and exit without changing anything.
     */
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

    /** Commits the staged files to the repository.
     *  <p>
     *  This method creates a new commit with the staged files and the given commit message.
     *  It updates the HEAD pointer to point to the new commit.
     *
     *  @param message the commit message
     *
     *  @implSpec Saves a snapshot of tracked files in the current commit and staging area
     *  so they can be restored at a later time, creating a new commit.
     *  The commit is said to be tracking the saved files.
     *  By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files;
     *  it will keep versions of files exactly as they are, and not update them.
     *  A commit will only update the contents of files it is tracking
     *  that have been staged for addition at the time of commit,
     *  in which case the commit will now include the version of the file that was staged
     *  instead of the version it got from its parent.
     *  A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     *  Finally, files tracked in the current commit may be untracked in the new commit
     *  as a result of being staged for removal by the rm command (below).
     *  <p>
     *  The bottom line: By default, a commit has the same file contents as its parent.
     *  Files staged for addition and removal are the updates to the commit.
     *  Of course, the date (and likely the message) will also differ from the parent.
     */
    public void commit(String message) {
        if (message == null || message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.stagedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // * create a new commit with the staged files and the given commit message
        Commit newCommit = new Commit(message, HEAD, stagingArea.getStagedFileBlobs(), true); // per spec, empty commits are the default
        // * update the HEAD pointer to point to the new commit
        HEAD = newCommit.getUid();
        try {
            Files.writeString(HEAD_FILE.toPath(), HEAD);
        } catch (IOException e) {
            throw error("Unable to write to HEAD file: " + e.getMessage());
        }
        // Per spec, no output on System.out, but we print the commit briefing to stderr
        System.err.println("[" + HEAD.substring(0, 7) + "] " + message);
        System.err.println(newCommit.getFileBlobs().size() + " files changed"); // summarize the changes
        // * clear the staging area
        stagingArea.stagedFiles.clear();
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
    }

    /** Restores a file from the current commit or the staging area.
     *  <p>
     *  This method restores a file to the working directory from the current commit
     *  or removes it from the staging area if it is staged for addition.
     *
     *  @param filename the name of the file to restore
     *
     *  @implSpec Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     *  If the file does not exist in the previous commit, abort,
     *  printing the error message {@code File does not exist in that commit.} Do not change the CWD.
     */
    public void restoreFile(String filename) {
        // * restore a file from the current commit
        if (filename == null || filename.isBlank()) {
            System.out.println("Please enter a file name.");
            return;
        }
        // // * check if the file is staged for addition
        // if (stagingArea.stagedFiles.containsKey(filename)) {
        //     stagingArea.stagedFiles.remove(filename); // remove it from the staging area
        //     writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
        //     System.err.println("File " + filename + " has been restored from the staging area.");
        //     return;
        // }
        // * check if the file is tracked in the current commit
        Commit currentCommit = Commit.getByUid(HEAD);
        if (!currentCommit.getFileBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob = Blob.getByUid(currentCommit.getFileBlobs().get(filename));
        if (blob == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        // * restore the file to the working directory
        File file = join(CWD, filename);
        writeContents(file, blob.getContents());
        System.err.println("File " + filename + " has been restored to the working directory.");
    }

    public void log() {
        // * print the commit history
        Commit currentCommit = Commit.getByUid(HEAD);
        Commit initialCommit = Commit.initialCommit();
        while (currentCommit != null) {
            System.out.println("===");
            System.err.println("logging the commit " + currentCommit.getUid());
            System.out.println("commit " + currentCommit.getUid());
            System.out.println("Date: " + currentCommit.timestamp);
            System.out.println(currentCommit.message);
            System.out.println();
            if (currentCommit.equals(initialCommit)){
                break; // reached the initial commit, stop logging
            }
            // currentCommit = (currentCommit.parents[0] == initialCommit.getUid())?
            //         initialCommit : Commit.getByUid(currentCommit.parents[0]);
            if (currentCommit.parents[0].equals(initialCommit.getUid())) {
                currentCommit = initialCommit;
            } else {
                currentCommit = Commit.getByUid(currentCommit.parents[0]);
            }
        }
    }
}
