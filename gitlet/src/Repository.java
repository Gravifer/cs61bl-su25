package gitlet;

// DONE: any imports you need here
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

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
    public static final File BRC_DIR = join(REF_DIR, "heads");
    protected final String defaultBranch; // default branch name

    /** The hashes various tags point to. */
    public static final File TAG_DIR = join(REF_DIR, "tags");

    /** The HEAD file saves the HEAD pointer UID */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    protected String HEAD;
    public String getHead() {
        return HEAD;
    }
    /**
     * The current HEAD commit object.
     * This is transient and not persisted; it is rebuilt on demand.
     */
    protected transient Commit HeadCommit;
    /**
     * Returns the current HEAD commit object, using the transient cache if available.
     * If HeadCommit is null, it will be loaded from HEAD and cached.
     * Always updates the cache to ensure consistency.
     */
    public Commit getHeadCommit() {
        if (HEAD == null || HEAD.isEmpty()) return null;
        if (HeadCommit == null || !HeadCommit.getUid().equals(HEAD)) {
            HeadCommit = Commit.getByUid(HEAD);
        }
        return HeadCommit;
    }
    public static String resolveHead(File file) {
        // * resolve the HEAD pointer to a full UID if it is a prefix
        if (!file.exists()) {
            throw error("HEAD file does not exist.");
        }
        String headContent = readContentsAsString(file);
        if (headContent.startsWith("ref: ")) {
            // * if HEAD points to a ref, read the ref file and return its content
            String refPath = headContent.substring(5).trim();
            File refFile = join(GITLET_DIR, refPath);
            if (!refFile.exists()) {
                throw error("Ref file does not exist: " + refPath);
            }
            return readContentsAsString(refFile);
        } else {
            // * if HEAD points to a commit, return the commit UID
            return headContent.trim();
        }
    }
    public static String resolveHead(){
        return resolveHead(HEAD_FILE);
    }
    protected void updateHead(Commit newHead){
        // * update the HEAD pointer to point to the new commit
        this.HEAD = newHead.getUid();
        if (HEAD_FILE.exists()) {
            writeContents(HEAD_FILE, HEAD); // write the new HEAD UID to the HEAD file
        } else {
            throw error("HEAD file does not exist.");
        }
        this.HeadCommit = newHead; // update the HeadCommit reference
    }
    protected void updateHead(String newHeadUid){
        // * update the HEAD pointer to point to the new commit
        Commit newHead = Commit.getByUid(newHeadUid);
        this.HEAD = newHeadUid;
        if (HEAD_FILE.exists()) {
            writeContents(HEAD_FILE, HEAD); // write the new HEAD UID to the HEAD file
        } else {
            throw error("HEAD file does not exist.");
        }
        this.HeadCommit = newHead; // update the HeadCommit reference
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
        this.defaultBranch = "main";
        this.stagingArea = new StagingArea();
    }
    protected Repository(StagingArea stagingArea) {
        // * constructor initializes the repository with a given staging area
        // * this is used for testing purposes
        this.HEAD = "";
        this.defaultBranch = "main";
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
    protected static void init_db(String branch, String defaultBranch) throws IOException {
        // HEAD cannot be used as a branch name
        if (branch == null || branch.isBlank()) {
            throw error("Branch name cannot be null or empty.");
        }
        if (branch.equals("HEAD") || defaultBranch.equals("HEAD")) {
            throw error("Branch name cannot be 'HEAD'.");
        }
        if (defaultBranch == null || defaultBranch.isBlank()) {
            defaultBranch = branch; // if no default branch is provided, use `branch` as the default
        }
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
                if (!Repository.INDX_FILE.createNewFile() && !Repository.INDX_FILE.createNewFile()) { // create the index file
                    throw error("Unable to create index file in .gitlet/ directory.");
                }
                Repository repo = new Repository(); // create a new repository instance
                writeObject(INDX_FILE, repo.stagingArea); // persist the staging area to the index file
                if (!Repository.DESC_FILE.createNewFile() && !Repository.DESC_FILE.createNewFile()) { // create the description file
                    throw error("Unable to create description file in .gitlet/ directory.");
                }
                writeContents(Repository.DESC_FILE, repo.description); // write the default description to the description file
                if (!Repository.HEAD_FILE.createNewFile() && !Repository.HEAD_FILE.createNewFile()) { // create the HEAD file
                    throw error("Unable to create HEAD file in .gitlet/ directory.");
                }
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
                writeContents(Repository.HEAD_FILE, initialCommit.getUid()); // write the initial commit UID to HEAD
                // DONE: create the default branch ("main")
                File currentBranchFile = join(Repository.BRC_DIR, branch);
                if (!currentBranchFile.createNewFile() && !currentBranchFile.createNewFile()) { // create the default branch file
                    throw error("Unable to create default branch ref file");
                }
                writeContents(currentBranchFile, initialCommit.getUid()); // write the initial commit UID to the default branch file
                if (!branch.equals(defaultBranch)) {
                    File defaultBranchFile = join(Repository.BRC_DIR, defaultBranch);
                    if (!defaultBranchFile.createNewFile() && !defaultBranchFile.createNewFile()) { // create the default branch file
                        throw error("Unable to create default branch ref file");
                    }
                    writeContents(defaultBranchFile, initialCommit.getUid()); // write the initial commit UID to the default branch file
                    // put the ref to the default branch in the HEAD file
                }
                // get the relative path from the GITLET_DIR to the branch file
                writeContents(Repository.HEAD_FILE, "ref: " + GITLET_DIR // point HEAD to the default branch
                        .toPath().relativize(currentBranchFile.toPath()).toString().replace("\\", "/"));
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
    protected static void init_db() throws IOException {
        // * initialize the repository with the default branch "main"
        init_db("main", "main");
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
        repo.HEAD = resolveHead();
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
        // if it is `add`ed, it should not be removed
        stagingArea.removedFiles.remove(filePath);
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
        // if it is already identical to what the current commit tracks, no need to stage it
        Commit headCommit = getHeadCommit();
        if (headCommit != null && headCommit.getFileBlobs().containsKey(filePath)) {
            String headBlobUid = headCommit.getFileBlobs().get(filePath);
            if (blob.getUid().equals(headBlobUid)) {
                stagingArea.stagedFiles.remove(filePath);
                writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
                return;
            }
        }
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
        Commit newCommit = new Commit(message, HEAD, stagingArea.getStagedFileBlobs(), stagingArea.getRemovedFileBlobs(), true); // per spec, empty commits are the default
        // * update the HEAD pointer to point to the new commit
        HEAD = newCommit.getUid();
        try {
            // if the HEAD file contains a ref, we should update it to point to the new commit
            if (HEAD_FILE.exists()) {
                String headContent = readContentsAsString(HEAD_FILE);
                if (headContent.startsWith("ref: ")) {
                    // * if HEAD points to a ref, we should update the ref file
                    String refPath = headContent.substring(5).trim();
                    File refFile = join(GITLET_DIR, refPath);
                    if (!refFile.exists()) {
                        throw error("Ref file does not exist: " + refPath);
                    }
                    writeContents(refFile, HEAD); // update the ref file to point to the new commit
                } else {
                    Files.writeString(HEAD_FILE.toPath(), HEAD);
                }
            } else {
                throw error("HEAD file does not exist.");
            }
        } catch (IOException e) {
            throw error("Unable to write to HEAD file: " + e.getMessage());
        }
        // Per spec, no output on System.out, but we print the commit briefing to stderr
        System.err.println("[" + HEAD.substring(0, 7) + "] " + message);
        System.err.println(newCommit.getFileBlobs().size() + " files changed"); // summarize the changes
        // * clear the staging area
        stagingArea.stagedFiles.clear();
        stagingArea.removedFiles.clear();
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
    }

    /** Restores a file from the current commit or the staging area.
     *  <p>
     *  This method restores a file to the working directory from the current commit
     *  or removes it from the staging area if it is staged for addition.
     *
     *  @param commitPrefix the commit prefix to restore from, or HEAD if null
     *  @param filename     the name of the file to restore
     *
     *  @implSpec Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     *  If the file does not exist in the previous commit, abort,
     *  printing the error message {@code File does not exist in that commit.} Do not change the CWD.
     */
    public void restoreFile(String commitPrefix, String filename) {
        if (filename == null || filename.isBlank()) {
            System.out.println("Please enter a file name.");
            return;
        }
        if (commitPrefix == null || commitPrefix.isBlank()) {
            System.err.println("restore: commitPrefix is null or empty, using HEAD as default.");
            commitPrefix = HEAD; // if no commit prefix is provided, use HEAD
        }
        Commit currentCommit = Commit.getByUid(commitPrefix);
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
    public void restoreFile(String filename) {
        restoreFile(HEAD, filename);
    }

    /** Logs the commit history of the repository.
     *  <p>
     *  This method prints the commit history starting from the current HEAD commit
     *  and going back to the initial commit.
     *  It formats the timestamp as local time with zone.
     *  @implSpec Starting at the current head commit, display information
     *  about each commit backwards along the commit tree until the initial commit,
     *  following the first parent commit links, ignoring any second parents found in merge commits.
     *  This set of commit nodes is called the commit’s history. For every node in this history,
     *  the information it should display is the commit id, the time the commit was made, and the commit message.
     */
    public void log() {
        // * print the commit history
        Commit currentCommit = getHeadCommit(); // Use cached HEAD commit
        Commit initialCommit = Commit.initialCommit();
        DateTimeFormatter formatter = DateTimeFormatter // Format timestamp as local time with zone
                .ofPattern("EEE MMM d HH:mm:ss yyyy XX", Locale.US).withZone(ZoneId.systemDefault()); // Local, according to Berkeley
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.getUid());
            // System.out.println("Date: " + currentCommit.timestamp);
            String formattedDate = formatter.format(currentCommit.timestamp); // Format the timestamp using Instant
            System.out.println("Date: " + formattedDate);

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

    public String currentBranch() {
        // * return the current branch name
        // * this is the branch that HEAD points to
        if (HEAD_FILE.exists()) {
            String headContent = readContentsAsString(HEAD_FILE);
            if (headContent.startsWith("ref: ")) {
                String refPath = headContent.substring(5).trim();
                File refFile = join(GITLET_DIR, refPath);
                if (refFile.exists()) {
                    return refFile.getName(); // return the branch name
                }
            } else {
                return "HEAD"; // HEAD is not pointing to a branch, return HEAD
            }
        }
        return defaultBranch; // default branch name
    }

    public boolean isDetachedHead() {
        // * check if the HEAD is detached
        // * this means that HEAD is not pointing to a branch, but to a commit
        if (HEAD_FILE.exists()) {
            String headContent = readContentsAsString(HEAD_FILE);
            return !headContent.startsWith("ref: "); // if HEAD does not start with "ref: ", it is detached
        }
        return false; // if the HEAD file does not exist, it is not detached
    }

    public void status() {
        // * print the status of the repository
        // * this includes the current branch, staged files, and untracked files
        // * the .gitlet/ directory should always be ignored
        System.out.println("=== Branches ===");
        // walk through the branches directory and print the branch names; needs to recurse to resolve branches with slashes
        File[] branches = BRC_DIR.listFiles();
        if (branches == null || branches.length == 0) {
            System.out.println("(none)");
        } else {
            for (File branch : branches) {
                String branchName = branch.getName();
                if (!isDetachedHead()) {
                    if (branchName.equals(currentBranch())) {
                        System.out.print("*"); // mark the current branch with an asterisk
                    } else {
                        System.out.print("");
                    }
                }
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        if (!stagingArea.stagedFiles.isEmpty()) {
            // for (Map.Entry<String, StagingArea.fileInfo> entry : stagingArea.stagedFiles.entrySet()) {
            //     System.out.println(entry.getKey());
            // }
            // files should be lexicographically sorted
            stagingArea.stagedFiles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println(entry.getKey()));
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (!stagingArea.removedFiles.isEmpty()) {
            // for (Map.Entry<String, StagingArea.fileInfo> entry : stagingArea.removedFiles.entrySet()) {
            //     System.out.println(entry.getKey());
            // }
            // files should be lexicographically sorted
            stagingArea.removedFiles.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.println(entry.getKey()));
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        // * print the unstaged files
        if (!stagingArea.unstagedFiles.isEmpty()) {
            // for (Map.Entry<String, StagingArea.fileInfo> entry : stagingArea.unstagedFiles.entrySet()) {
            //     System.out.println(entry.getKey());
            // }
            // files should be lexicographically sorted
            stagingArea.unstagedFiles.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.println(entry.getKey()));
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        // Print untracked files: files in CWD that are not staged, not unstaged, not removed, and not tracked by the current commit
        Commit currentCommit = getHeadCommit(); // Use cached HEAD commit
        Set<String> trackedFiles = currentCommit.getFileBlobs().keySet();
        File[] untrackedFiles = CWD.listFiles((dir, name) -> !name.equals(".gitlet")
                && !stagingArea.stagedFiles.containsKey(name)
                && !stagingArea.unstagedFiles.containsKey(name)
                && !stagingArea.removedFiles.containsKey(name)
                && !trackedFiles.contains(name));
        if (untrackedFiles != null && untrackedFiles.length > 0) {
            // Sort files lexicographically
            Arrays.stream(untrackedFiles).sorted(File::compareTo).forEach(file -> System.out.println(file.getName()));

        }
    }

    /** Removes files from the staging area and working directory.
     *  <p>
     *  This method removes the specified files from the staging area and working directory.
     *  If a file is staged, it will be removed from the staging area.
     *  If a file is not staged, it will be removed from the working directory.
     *  If a file is not tracked, it will be ignored.
     *
     *  @param filename the name of the file to remove
     *
     *  @implSpec Unstage the file if it is currently staged for addition.
     *  If the file is tracked in the current commit, stage it for removal
     *  and remove the file from the working directory
     *  if the user has not already done so. (Do not remove it unless it is tracked in the current commit.)
     *  <p>
     *  If the file is not staged for addition and not tracked by the head commit,
     *  print the error message {@code No reason to remove the file.}
     */
    public void removeFile(String filename) {
        // * remove a file from the staging area and working directory
        if (filename == null || filename.isBlank()) {
            System.out.println("Please enter a file name.");
            return;
        }
        File file = join(CWD, filename);
        if (!file.exists()) {
            throw error("File does not exist: " + filename);
        }
        // * if the file is staged, unstage it
        if (stagingArea.stagedFiles.containsKey(filename)) {
            stagingArea.stagedFiles.remove(filename);
            writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
            System.err.println("Unstaged " + filename + ".");
            return;
        }
        // * if the file is tracked in the current commit, stage it for removal
        Commit currentCommit = getHeadCommit(); // Use cached HEAD commit
        if (currentCommit.getFileBlobs().containsKey(filename)) {
            stagingArea.removedFiles.put(filename, new StagingArea.fileInfo(filename, currentCommit.getFileBlobs().get(filename),
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(), file.length()));
            writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
            try {
                Files.delete(file.toPath()); // remove the file from the working directory
                System.err.println("Removed " + filename + ".");
            } catch (IOException e) {
                System.err.println("Failed to remove " + filename + ": " + e.getMessage());
            }
            return;
        }
        // * if the file is not staged for addition and not tracked by the head commit, print an error message
        System.out.println("No reason to remove the file: " + filename);
    }
}
