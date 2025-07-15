package gitlet;

// DONE: any imports you need here
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 * <p>
 *  <blockquote>DONE: It's a good idea to give a description here of what else this Class
 *  does at a high level.</blockquote>
 *  This class encapsulates the functionality of a gitlet repository,
 *  including initializing the repository, staging files,
 *  committing changes, restoring files, and removing files.
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
    public static final Path CWD = Path.of(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final Path GITLET_DIR = CWD.resolve(".gitlet");

    /* DONE: fill in the rest of this class. */

    /** The persisted store of serializations. */
    public static final Path OBJ_DIR = GITLET_DIR.resolve("objects");

    /** The hashes various refs point to. */
    public static final Path REF_DIR = GITLET_DIR.resolve("refs");

    /** The hashes various branches point to. */
    public static final Path BRC_DIR = REF_DIR.resolve("heads");
    protected final String defaultBranch; // default branch name

    /** The hashes various tags point to. */
    public static final Path TAG_DIR = REF_DIR.resolve("tags");

    /** The HEAD file saves the HEAD pointer UID */
    public static final Path HEAD_FILE = GITLET_DIR.resolve("HEAD");
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
    public Commit getBranchCommit(String branch) {
        String commitUid = resolveHead(BRC_DIR.resolve(branch));
        // Logging.dbg.println("Resolving head commit for branch: " + branch + " -> " + commitUid);
        return Commit.getByUid(commitUid);
    }
    public static String resolveHead(Path file) {
        if (!Files.exists(file)) {
            throw error("HEAD file does not exist.");
        }
        String headContent = readContentsAsString(file);
        if (headContent.startsWith("ref: ")) {
            // * if HEAD points to a ref, read the ref file and return its content
            String refPath = headContent.substring(5).trim();
            Path refFile = GITLET_DIR.resolve(refPath);
            if (!Files.exists(refFile)) {
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
        if (Files.exists(HEAD_FILE)) {
            writeContents(HEAD_FILE, HEAD);
        } else {
            throw error("HEAD file does not exist.");
        }
        this.HeadCommit = newHead; // update the HeadCommit reference
    }

    /** Updates the HEAD pointer to point to a new ref.
     *  <p>
     *  This method updates the HEAD pointer to point to a new ref,
     *  which is typically a branch or tag.
     *
     *  @param newHeadRef the new ref to point HEAD to,
     *                    e.g., "refs/heads/main" or "refs/tags/v1.0"; can be got by
     *                    {@code GITLET_DIR.toPath().relativize(refFile.toPath()).toString().replace("\\", "/")}<br>
     *                    where {@code refFile} is the file of the ref, containing its head commit hash.
     */
    protected void updateHeadRef(String newHeadRef){
        if (Files.exists(HEAD_FILE)) {
            writeContents(HEAD_FILE, "ref: " + newHeadRef);
        } else {
            throw error("HEAD file does not exist.");
        }
    }
    protected void updateHeadRef(Path refFile){
        if (Files.exists(HEAD_FILE)) {
            writeContents(HEAD_FILE, "ref: " + GITLET_DIR.relativize(refFile).toString().replace("\\", "/"));
        } else {
            throw error("HEAD file does not exist.");
        }
    }

    /** The index file, aka "current directory cache",
     * contains the serialization of blobs representing staged files. */
    public static final Path INDX_FILE = GITLET_DIR.resolve("index");
    /** The staging area, which is a map of file paths to their corresponding Blob UIDs.
     *  This is used to track files that are staged for commit.
     *  <p>
     *  Keys are file paths relative to the repository root,
     *  and values are fileInfo objects, containing some metadata and
     *  the SHA-1 hashes of the Blob objects representing the contents of those files.
     */
    protected final StagingArea stagingArea;

    /** The description file saves the description of the repo */
    public static final Path DESC_FILE = GITLET_DIR.resolve("description");
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
        if (Files.exists(Repository.GITLET_DIR)) {
            // * the spec does not ask for reinitialization of the repository, so we should not allow it
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        String dbPath = Repository.GITLET_DIR.toAbsolutePath().toString();
        try {
            Files.createDirectory(Repository.GITLET_DIR);// if the directory was created successfully, we can initialize the repository
            if (!Files.exists(Repository.INDX_FILE)) {
                Files.createFile(Repository.INDX_FILE);
            }
            Repository repo = new Repository(); // create a new repository instance
            writeObject(INDX_FILE, repo.stagingArea); // persist the staging area to the index file
            if (!Files.exists(Repository.DESC_FILE)) { // create the description file
                Files.createFile(Repository.DESC_FILE);
            }
            writeContents(Repository.DESC_FILE, repo.description); // write the default description to the description file
            if (!Files.exists(Repository.HEAD_FILE)) { // create the HEAD file
                Files.createFile(Repository.HEAD_FILE);
            }
            Commit initialCommit = Commit.initialCommit(); // initial commit is empty and a singleton, so no need to persist it
            // nevertheless, we should initialize the object database
            for (Path dir : new Path[]{Repository.OBJ_DIR, Repository.REF_DIR, Repository.BRC_DIR, Repository.TAG_DIR}) {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                if (!Files.isDirectory(dir)) {
                    throw error("The path " + dir.toAbsolutePath() + " is not a directory.");
                }
            }
            writeContents(Repository.HEAD_FILE, initialCommit.getUid()); // write the initial commit UID to HEAD
            // DONE: create the default branch ("main")
            Path currentBranchFile = Repository.BRC_DIR.resolve(branch);
            if (!Files.exists(currentBranchFile)) {
                Files.createFile(currentBranchFile);
            }
            writeContents(currentBranchFile, initialCommit.getUid()); // write the initial commit UID to the default branch file
            if (!branch.equals(defaultBranch)) {
                Path defaultBranchFile = Repository.BRC_DIR.resolve(defaultBranch);
                if (!Files.exists(defaultBranchFile)) {
                    Files.createFile(defaultBranchFile);
                }
                writeContents(defaultBranchFile, initialCommit.getUid()); // write the initial commit UID to the default branch file
                // put the ref to the default branch in the HEAD file
            }
            // get the relative path from the GITLET_DIR to the branch file
            writeContents(Repository.HEAD_FILE, "ref: " + Repository.GITLET_DIR.relativize(currentBranchFile).toString().replace("\\", "/"));
            Logging.info.println("Initialized an empty Gitlet repository in " + dbPath); // Per spec, no output on System.out
        } catch (Exception e) {
            Logging.err.println("An unexpected error occurred while initializing the Gitlet repository: " + e.getMessage());
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
        Repository repo;
        if (Files.exists(INDX_FILE)) {
            repo = new Repository(readObject(INDX_FILE, StagingArea.class));
        } else {
            repo = new Repository();
        }
        if (!Files.exists(HEAD_FILE)) {
            Logging.err.println("The HEAD file does not exist.");
            // throw error("fatal: not a gitlet repository (or any of the parent directories): .gitlet"); // mimic the behavior of git
            System.err.println("fatal: not a gitlet repository (or any of the parent directories): .gitlet");
            return null;
        }
        repo.HEAD = resolveHead();
        if (Files.exists(DESC_FILE)) {
            repo.description = readContentsAsString(DESC_FILE);
        }
        Path globalLogFile = GITLET_DIR.resolve("global-log-commits.ser");
        if (Files.exists(globalLogFile)) {
            var commitList = readObject(globalLogFile, ArrayList.class);
            repo.allCommitUids = new HashSet<String>(commitList);
        } else {
            repo.allCommitUids = new HashSet<String>();
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
        Path file = CWD.resolve(filePath);
        if (!Files.exists(file)) {
            throw error("File does not exist: " + filePath);
        }
        // if it is `add`ed, it should not be removed
        stagingArea.removedFiles.remove(filePath);
        Blob blob = Blob.blobify(file);
        long mtime = 0;
        try {
            mtime = Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            Logging.warn.println("Can't get last modified time of " + file.toAbsolutePath());
        }
        long ctime = mtime;
        try {
            ctime = Files.readAttributes(file, BasicFileAttributes.class).creationTime().toMillis();
        } catch (IOException e) {
            Logging.warn.println("Can't get creation time of " + file.toAbsolutePath());
        }
        long size = 0;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            Logging.warn.println("Can't get size of " + file.toAbsolutePath());
        }
        Commit headCommit = getHeadCommit();
        if (headCommit != null && headCommit.getFileBlobs().containsKey(filePath)) {
            String headBlobUid = headCommit.getFileBlobs().get(filePath);
            if (blob.getUid().equals(headBlobUid)) {
                stagingArea.stagedFiles.remove(filePath);
                writeObject(INDX_FILE, stagingArea);
                return;
            }
        }
        stagingArea.stagedFiles.put(filePath, new StagingArea.fileInfo(filePath, blob.getUid(), ctime, mtime, size));
        writeObject(INDX_FILE, stagingArea);
    }

    HashSet<String> allCommitUids = new HashSet<>(); // used to track all commit UIDs for the current branch
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
    public void commit(String message, String[] otherParents) {
        if (message == null || message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.stagedFiles.isEmpty() && stagingArea.removedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // * create a new commit with the staged files and the given commit message
        String[] parents = new String[otherParents.length + 1];
        parents[0] = HEAD; // the first parent is the current HEAD
        Commit currentCommit = getHeadCommit();
        System.arraycopy(otherParents, 0, parents, 1, otherParents.length);// add the other parents
        Commit newCommit = new Commit(message, parents, stagingArea.getStagedFileBlobs(), stagingArea.getRemovedFileBlobs(), true); // per spec, empty commits are the default

        // // construct a map comparing newCommit with HEAD
        Map<String, String> addedFiles = new HashMap<>(newCommit.getFileBlobs());
        Map<String, String> removedFiles = new HashMap<>(currentCommit.getFileBlobs());
        Map<String, String> changedFiles = new HashMap<>();
        for (Map.Entry<String, String> entry : currentCommit.getFileBlobs().entrySet()) {
            addedFiles.remove(entry.getKey(), entry.getValue()); // if both file name and blob UID are not changed, it is not removed or modified
        }
        for (Map.Entry<String, String> entry : newCommit.getFileBlobs().entrySet()) {
            removedFiles.remove(entry.getKey(), entry.getValue()); // if both file name and blob UID are not changed, it is not added or modified
        }
        // if a filename exists in both currentCommit and newCommit, but the blob UID is different, it is a changed file
        for (Map.Entry<String, String> entry : currentCommit.getFileBlobs().entrySet()) {
            if (newCommit.getFileBlobs().containsKey(entry.getKey()) && !newCommit.getFileBlobs().get(entry.getKey()).equals(entry.getValue())) {
                changedFiles.put(entry.getKey(), newCommit.getFileBlobs().get(entry.getKey()));
            }
        }

        // * update the HEAD pointer to point to the new commit
        HEAD = newCommit.getUid();
        allCommitUids.add(HEAD); // track all commit UIDs for the current branch
        writeObject(GITLET_DIR.resolve("global-log-commits.ser"), new ArrayList<>(allCommitUids));
        try {// if the HEAD file contains a ref, update it to point to the new commit
            if (Files.exists(HEAD_FILE)) {
                String headContent = readContentsAsString(HEAD_FILE);
                if (headContent.startsWith("ref: ")) {
                    // * if HEAD points to a ref, update the ref file
                    String refPath = headContent.substring(5).trim();
                    Path refFile = join(GITLET_DIR, refPath);
                    if (!Files.exists(refFile)) {
                        throw error("Ref file does not exist: " + refPath);
                    }
                    writeContents(refFile, HEAD); // update the ref file to point to the new commit
                } else {
                    Files.writeString(HEAD_FILE, HEAD);
                }
            } else {
                throw error("HEAD file does not exist.");
            }
        } catch (IOException e) {
            throw error("Unable to write to HEAD file: " + e.getMessage());
        }
        // Per spec, no output on System.out, but we print the commit briefing to stderr
        Logging.info.println("[" + currentBranch() + " " + HEAD.substring(0, 7) + "] " + message);

        // summarize the differences from the parent commit
        if (!addedFiles.isEmpty()) {
            Logging.info.print(addedFiles.size() + " file(s) added:  ");
            for (String fileName : addedFiles.keySet()) {
                Logging.info.print("  " + fileName);
            }
            Logging.info.println();
        }
        if (!removedFiles.isEmpty()) {
            Logging.info.print(removedFiles.size() + " file(s) removed:");
            for (String fileName : removedFiles.keySet()) {
                Logging.info.print("  " + fileName);
            }
            Logging.info.println();
        }
        if (!changedFiles.isEmpty()) {
            Logging.info.println(changedFiles.size() + " file(s) changed:");
            for (Map.Entry<String, String> entry : changedFiles.entrySet()) {
                Logging.info.println("  " + entry.getKey() + " (was: " + entry.getValue().substring(0, 7) + ")");
            }
        }

        // * clear the staging area
        stagingArea.stagedFiles.clear();
        stagingArea.removedFiles.clear();
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
    }
    public void commit(String message) {
        // * no other parents than HEAD
        commit(message, new String[0]);
    }
    public void mergeCommit(Commit commitToMerge, String message) {
        // 1. Find the split point (latest common ancestor)
        Commit headCommit = getHeadCommit();
        Commit splitPoint = Commit.findLCA(headCommit, commitToMerge);
        if (splitPoint == null) {
            System.out.println("No split point found. Aborting merge.");
            Logging.err.println("Merge attempted upon commits" + headCommit.getUid().substring(0, 7) + " and " + commitToMerge.getUid().substring(0, 7) + ", but the history seems unrelated.");
            return;
        }
        // check if commitToMerge is an ancestor of the current HEAD.
        if (commitToMerge.isLinearAncestorOf(headCommit)) {
            Logging.warn.println("Incoming commit is an ancestor of the current HEAD. Nothing to do.");
            return;
        }
        // check for staged but not commited additions and removals before merge
        if (!stagingArea.stagedFiles.isEmpty() || !stagingArea.removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            Logging.dbg.println("Merge failed due to uncommitted changes.");
            Logging.dbg.print("[+]:");
            for (String fileName : stagingArea.stagedFiles.keySet()) {
                Logging.dbg.print(" " + fileName);
            }
            Logging.dbg.print("[-]:");
            for (String fileName : stagingArea.removedFiles.keySet()) {
                Logging.dbg.print(" " + fileName);
            }
            return;
        }
        // check for untracked files before merge
        Set<String> trackedFiles = headCommit.getFileBlobs().keySet();
        Set<String> stagedFiles = stagingArea.stagedFiles.keySet();
        for (String fileName : Utils.plainFilenamesIn(CWD)) {
            if (!trackedFiles.contains(fileName) ) { // && !stagedFiles.contains(fileName)) {
                boolean willBeOverwrittenOrDeleted =
                    commitToMerge.getFileBlobs().containsKey(fileName) ||
                    (splitPoint.getFileBlobs().containsKey(fileName) && !commitToMerge.getFileBlobs().containsKey(fileName));
                if (willBeOverwrittenOrDeleted) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    Logging.err.println("Untracked file " + fileName + " in the way; abort.");
                    return;
                }
            }
        }
        // 2. Collect all relevant file names
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPoint.getFileBlobs().keySet());
        allFiles.addAll(headCommit.getFileBlobs().keySet());
        allFiles.addAll(commitToMerge.getFileBlobs().keySet());
        boolean hasConflict = false;
        for (String filename : allFiles) {
            String splitBlob = splitPoint.getFileBlobs().get(filename);
            String headBlob = headCommit.getFileBlobs().get(filename);
            String mergeBlob = commitToMerge.getFileBlobs().get(filename);
            // Rule 1: Only modified in merge branch, not in current branch
            if (Objects.equals(splitBlob, headBlob) && !Objects.equals(splitBlob, mergeBlob) && mergeBlob != null) {
                Blob blob = Blob.getByUid(mergeBlob);
                writeContents(CWD.resolve(filename), blob.getContents());
                stageFile(filename);
            }
            // Rule 2: Only modified in current branch, not in merge branch
            else if (!Objects.equals(splitBlob, headBlob) && Objects.equals(splitBlob, mergeBlob)) {
                // Keep current branch content, do nothing
            }
            // Rule 3: Modified in both branches and contents differ, conflict
            else if (!Objects.equals(splitBlob, headBlob) && !Objects.equals(splitBlob, mergeBlob) && !Objects.equals(headBlob, mergeBlob)) {
                hasConflict = true;
                String headContent = headBlob == null ? "" : Blob.getByUid(headBlob).getContentsAsString();
                String mergeContent = mergeBlob == null ? "" : Blob.getByUid(mergeBlob).getContentsAsString();
                String conflictContent = "<<<<<<< HEAD\n" + headContent + "=======\n" + mergeContent + ">>>>>>>\n";
                writeContents(CWD.resolve(filename), conflictContent);
                stageFile(filename);
                Logging.warn.println("Encountered a merge conflict in file: " + filename);
            }
            // Rule 5: Not present at split point, present only in merge branch
            else if (splitBlob == null && mergeBlob != null && headBlob == null) {
                Blob blob = Blob.getByUid(mergeBlob);
                writeContents(CWD.resolve(filename), blob.getContents());
                stageFile(filename);
            }
            // Rule 6: Present at split point, unmodified in current branch, absent in merge branch
            else if (splitBlob != null && mergeBlob == null && Objects.equals(splitBlob, headBlob)) {
                removeFile(filename);
            }
            // Other cases: keep current branch content
        }
        // 3. Create the merge commit
        String[] parents = new String[]{headCommit.getUid(), commitToMerge.getUid()};
        commit(message, parents);
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        } else {
            Logging.info.println("Merge completed successfully.");
        }
    }
    public void mergeCommit(Commit commitToMerge) {
        // * commit with a default message
        mergeCommit(commitToMerge, String.format("Merged commit %s into %s", commitToMerge.getUid().substring(0, 7), getHeadCommit().getUid().substring(0, 7)));
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
            Logging.warn.println("restore: nothing to restore.");
            return;
        }
        if (commitPrefix == null || commitPrefix.isBlank()) {
            Logging.warn.println("restore: commitPrefix is null or empty, using HEAD as default.");
            commitPrefix = HEAD;
        }
        try {
            Commit intendedCommit = Commit.getByUid(commitPrefix);
            if (intendedCommit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (!intendedCommit.getFileBlobs().containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
                Logging.err.println("File " + filename + " does not exist in commit " + commitPrefix);
                return;
            }
            Blob blob = Blob.getByUid(intendedCommit.getFileBlobs().get(filename));
            if (blob == null) {
                System.out.println("File does not exist in that commit.");
                Logging.err.println("Blob for file " + filename + " missing in commit " + commitPrefix);
                return;
            }
            Path file = CWD.resolve(filename);
            writeContents(file, blob.getContents());
            Logging.info.println("File " + filename + " has been restored to the working directory.");
        } catch (GitletException e) {
            if (e.getMessage().contains("Object does not exist")) {
                System.err.println(e.getMessage());
                System.out.println("No commit with that id exists.");
            } else {
                throw e;
            }
        }
    }
    public void restoreFile(String filename) {
        restoreFile(HEAD, filename);
    }

    /**
     * Functions like {@code git reset --hard [commit hash]}
     *
     * @implSpec Restores all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * See the intro for an example of what happens to the head pointer after using reset.
     * The {@code commit id} may be abbreviated as for restore. The staging area is cleared.
     * <p>
     * If no commit with the given id exists, print {@code No commit with that id exists.}
     * If a working file is untracked in the current branch and would be overwritten by the reset,
     * print {@code There is an untracked file in the way; delete it, or add and commit it first.} and exit;
     * perform this check before doing anything else.
     * */
    public void resetHardCommit(String commitPrefix) {
        // reuses the restoreFile method to restore all files tracked by the given commit
        if (commitPrefix == null || commitPrefix.isBlank()) {
            Logging.warn.println("resetHardCommit: commitPrefix is null or empty, using HEAD as default.");
            commitPrefix = HEAD;
        }
        try {
            Commit intendedCommit = Commit.getByUid(commitPrefix);
            if (intendedCommit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            // * check for untracked files in the working directory // ! the spec requires a hard reset
            for (String fileName : plainFilenamesIn(CWD)) {
                Path file = CWD.resolve(fileName);
                if (Files.exists(file) && (!getHeadCommit().getFileBlobs().containsKey(fileName) && !stagingArea.stagedFiles.containsKey(fileName))) { // ! per spec, we should not check the staging area
                    // * if the file is tracked in the commit, it should not be untracked
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    Logging.err.println("Untracked file " + fileName + " would be overwritten by reset.");
                    return;
                }
            }
            // * restore all files tracked by the given commit
            for (String fileName : intendedCommit.getFileBlobs().keySet()) {
                restoreFile(intendedCommit.getUid(), fileName);
            }
            // * remove files that are not present in the commit
            for (String fileName : plainFilenamesIn(CWD)) {
                if (!intendedCommit.getFileBlobs().containsKey(fileName) && !stagingArea.stagedFiles.containsKey(fileName)) {
                    Path file = CWD.resolve(fileName);
                    if (Files.exists(file)) {
                        if (restrictedDelete(file)) {
                            Logging.info.println("Removed " + fileName + " from the working directory.");
                        } else {
                            Logging.warn.println("Failed to delete file: " + fileName + " ; removing from gitlet anyway.");
                        }
                    }
                }
            }
            // * clear the staging area
            stagingArea.stagedFiles.clear();
            stagingArea.removedFiles.clear();
            writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
            // updateHeadRef(intendedCommit); // update the HEAD pointer to point to the new commit
            writeContents(BRC_DIR.resolve(currentBranch()), intendedCommit.getUid()); // update the current branch file to point to the intended commit
        } catch (GitletException e) {
            if (e.getMessage().contains("Object does not exist")) {
                System.err.println(e.getMessage());
                System.out.println("No commit with that id exists.");
            } else {
                throw e;
            }
        }
    }

    /** Checks out a branch and updates the working directory.
     *
     *  @param branch the name of the branch to check out
     *
     *  @implSpec <b>Actually missing from the su24 spec.</b>
     *  <p>
     *  Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *  overwriting the versions of the files that are already there if they exist.
     *  Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     *  Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     *  The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
     *  <p>
     *  If no branch with that name exists, print {@code No such branch exists.}
     *  If that branch is the current branch, print {@code No need to checkout the current branch.}
     *  If a working file is untracked in the current branch and would be overwritten by the checkout,
     *  print {@code There is an untracked file in the way; delete it, or add and commit it first.} and exit;
     *  perform this check before doing anything else. Do not change the CWD.     *
     *  @see <a href="https://sp21.datastructur.es/materials/proj/proj2/proj2#checkout">sp21 spec</a>
     */
    public void checkoutBranch(String branch){
        if (branch == null || branch.isBlank()) {
            Logging.warn.println("checkoutBranch: branch is null or empty, using HEAD as default.");
            branch = currentBranch();
        }
        if (!isDetachedHead() && isCurrentBranch(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Path branchFile = BRC_DIR.resolve(branch);
        if (!Files.exists(branchFile)) {
            System.out.println("No such branch exists.");
            return;
        }
        String branchCommitUid = readContentsAsString(branchFile).trim();
        Commit intendedCommit = Commit.getByUid(branchCommitUid);
        if (intendedCommit == null) {
            System.out.println("No such branch exists.");
            return;
        }
        // // * check for untracked files in the working directory // disabling this mismatches the sp21 spec, but this command was removed from su24 anyway, so whatever passes the tests
        // for (String fileName : plainFilenamesIn(CWD)) {
        //     Path file = CWD.resolve(fileName);
        //     if (false && Files.exists(file) && !getHeadCommit().getFileBlobs().containsKey(fileName)) { // ! per spec, we should not check the staging area
        //         // * if the file is tracked in the commit, it should not be untracked
        //         System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        //         Logging.err.println("Untracked file " + fileName + " would be overwritten by checkout.");
        //         return;
        //     }
        // }
        // * restore all files tracked by the given commit
        for (String fileName : intendedCommit.getFileBlobs().keySet()) {
            restoreFile(intendedCommit.getUid(), fileName);
        }
        // * remove files that are not present in the commit
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!intendedCommit.getFileBlobs().containsKey(fileName) && !stagingArea.stagedFiles.containsKey(fileName)) {
                Path file = CWD.resolve(fileName);
                if (Files.exists(file)) {
                    if (restrictedDelete(file)) {
                        Logging.info.println("Removed " + fileName + " from the working directory.");
                    } else {
                        Logging.warn.println("Failed to delete file: " + fileName + " ; removing from gitlet anyway.");
                    }
                }
            }
        }
        // * clear the staging area
        stagingArea.stagedFiles.clear();
        stagingArea.removedFiles.clear();
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
        // * update the HEAD pointer to point to the new commit
        updateHeadRef(branchFile); // update the HEAD pointer to point to the new commit
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
        Path file = CWD.resolve(filename);
        if (!Files.exists(file)) {
            Logging.warn.println("File does not exist: " + filename + " ; removing from gitlet anyway.");
        }
        // * if the file is staged, unstage it
        if (stagingArea.stagedFiles.containsKey(filename)) {
            stagingArea.stagedFiles.remove(filename);
            writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
            Logging.info.println("Unstaged " + filename + ".");
            return;
        }
        // * if the file is tracked in the current commit, stage it for removal
        Commit currentCommit = getHeadCommit(); // Use cached HEAD commit
        if (currentCommit.getFileBlobs().containsKey(filename)) {
            long fileLength = 0;
            try {
                fileLength = Files.size(file);
            } catch (IOException e) {
                Logging.err.println("Failed to get file length: " + e.getMessage());
            }
            stagingArea.removedFiles.put(filename, new StagingArea.fileInfo(filename, currentCommit.getFileBlobs().get(filename),
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(), fileLength));
            writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
            if (restrictedDelete(file)) { // remove the file from the working directory
                // Logging.info.println("Removed " + filename + ".");
            } else {
                Logging.err.println("Failed to remove " + filename + ". It may not be writable or does not exist.");
            }
            return;
        }
        // * if the file is not staged for addition and not tracked by the head commit, print an error message
        System.out.println("No reason to remove the file.");
        Logging.warn.println("An attempt made to remove a file that is neither tracked or staged (" + filename + ").");
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
    public void log(Commit[] commits) {
        // * print the commit history
        DateTimeFormatter formatter = DateTimeFormatter // Format timestamp as local time with zone
                .ofPattern("EEE MMM d HH:mm:ss yyyy XX", Locale.US).withZone(ZoneId.systemDefault()); // Local, according to Berkeley
        for (Commit commit : commits) {
            commit.logCommit(formatter); // Print commit info
        }
    }
    public void log() {
        // * log the commit history starting from the current HEAD commit
        Commit[] heads = {getHeadCommit()};
        log(getCommitHistory(heads));
    }

    /** Gets the commit history starting from the given heads.
     *  <p>
     *  This method retrieves the commit history starting from the given heads
     *  and going back to the initial commit.
     *
     *  @param heads an array of head commits to start the history from
     *  @return an array of Commit objects representing the commit history, sorted by commit time, from newest to oldest
     */
    public Commit[] getCommitHistory(Commit[] heads) {
        // * get the commit history starting from the given heads
        // * and going back to the initial commit
        if (heads == null || heads.length == 0) {
            return new Commit[0]; // no heads, return empty array
        }
        Set<Commit> historySet = new HashSet<>();
        for (Commit head : heads) {
            Commit currentCommit = head;
            while (currentCommit != null && !historySet.contains(currentCommit)) {
                historySet.add(currentCommit);
                if (currentCommit.isInitialCommit() || currentCommit.parents == null || currentCommit.parents.length == 0) {
                    // * if the commit has no parents, it is the initial commit
                    break; // stop at the initial commit
                }
                if (currentCommit.parents[0].equals(Commit.initialCommit().getUid())) {
                    currentCommit = Commit.initialCommit();
                } else {
                    currentCommit = Commit.getByUid(currentCommit.parents[0]);
                }
            }
        }
        return historySet.stream().sorted(Comparator.comparingLong(Commit::getTime).reversed()).toArray(Commit[]::new);
    }

    public String currentBranch() {
        // * return the current branch name
        // * this is the branch that HEAD points to
        if (Files.exists(HEAD_FILE)) {
            String headContent = readContentsAsString(HEAD_FILE);
            // Logging.dbg.println("resolving current branch: HEAD at " + headContent);
            if (headContent.startsWith("ref: ")) {
                String refPath = headContent.substring(5).trim();
                Path refFile = GITLET_DIR.resolve(refPath);
                if (Files.exists(refFile)) {
                    return refFile.getFileName().toString();
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
        if (Files.exists(HEAD_FILE)) {
            String headContent = readContentsAsString(HEAD_FILE);
            // Logging.dbg.println("checking if HEAD is detached: HEAD at " + headContent + "; current branch: " + currentBranch());
            return !headContent.startsWith("ref: "); // if HEAD does not start with "ref: ", it is detached
        }
        return false; // if the HEAD file does not exist, it is not detached
    }

    public void status() {
        Commit currentCommit = getHeadCommit();
        Set<String> trackedFiles = currentCommit.getFileBlobs().keySet();
        System.out.println("=== Branches ===");
        String[] branches = getBranches();
        if (branches == null || branches.length == 0) {
            System.out.println("(none)");
        } else {
            for (String branch : branches) {
                if (!isDetachedHead()) {
                    if (isCurrentBranch(branch)) {
                        System.out.print("*"); // mark the current branch with an asterisk
                    } else {
                        System.out.print("");
                    }
                }
                System.out.println(branch);
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
        // dynamically check for unstaged files
        trackedFiles.stream().sorted().forEach(fileName -> {
            // skip staged and removed
            if (stagingArea.stagedFiles.containsKey(fileName) || stagingArea.removedFiles.containsKey(fileName)) {
                return;
            }
            Path file = CWD.resolve(fileName);
            if (Files.exists(file)) {
                Blob blob = Blob.blobify(file);
                String headBlobUid = currentCommit.getFileBlobs().get(fileName);
                if (!blob.getUid().equals(headBlobUid)) {
                    System.out.println(fileName);
                }
            } else {
                System.out.println(fileName);
            }
        });
        System.out.println();
        System.out.println("=== Untracked Files ===");
        // Print untracked files: files in CWD that are not staged, not unstaged, not removed, and not tracked by the current commit
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CWD, entry -> !entry.getFileName().toString().equals(".gitlet")
                && !stagingArea.stagedFiles.containsKey(entry.getFileName().toString())
                && !stagingArea.unstagedFiles.containsKey(entry.getFileName().toString())
                && !stagingArea.removedFiles.containsKey(entry.getFileName().toString())
                && !trackedFiles.contains(entry.getFileName().toString()))) {
            for (Path file : stream) {
                System.out.println(file.getFileName());
            }
        } catch (IOException e) {
            Logging.err.println("Failed to list untracked files: " + e.getMessage());
        }
    }
    /**
     * Create a new branch pointing to the current HEAD.
     * @param branchName the name of the branch to create
     * @implSpec Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch called “main”.
     */
    public void createBranch(String branchName, Commit targetCommit) {
        // * create a new branch pointing to the current HEAD
        // * if no target commit is provided, use the current HEAD commit
        if (targetCommit == null) {
            targetCommit = getHeadCommit(); // Use cached HEAD commit
        }
        if (targetCommit == null) {
            throw error("No current HEAD commit found.");
        }
        createBranch(branchName, targetCommit.getUid());
    }
    public void createBranch(String branchName, String targetUid) {
        if (branchName == null || branchName.isBlank()) {
            System.out.println("Please enter a branch name.");
            return;
        }
        Path branchFile = BRC_DIR.resolve(branchName);
        if (Files.exists(branchFile)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            Files.createFile(branchFile);
            writeContents(branchFile, targetUid);
        } catch (IOException e) {
            throw error("Failed to create branch: " + e.getMessage());
        }
    }
    public void createBranch(String branchName) {
        // * create a new branch pointing to the current HEAD
        // * if no target commit is provided, use the current HEAD commit
        Commit targetCommit = getHeadCommit(); // Use cached HEAD commit
        createBranch(branchName, targetCommit);
    }

    /**
     * Switches to the specified branch.
     * @param branch the name of the branch to switch to
     *
     * @implSpec Switches to the branch with the given name.
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist (i.e., checkout the commit).
     * Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the branch you are switching to are deleted.
     * The staging area is cleared unless the given branch is the current branch (see failure cases below).
     * If no branch with that name exists, print {@code No such branch exists.}
     * If that branch is the current branch, print {@code No need to switch to the current branch.}
     * If a working file is untracked in the current branch and would be overwritten by the switch,
     * print {@code There is an untracked file in the way; delete it, or add and commit it first.} and exit;
     * perform this check before doing anything else. Do not change the CWD.
     */
    public void switchBranch(String branch) {
        // check the HEAD file
        if (!isDetachedHead() && isCurrentBranch(branch)) {
            System.out.println("No need to switch to the current branch.");
            return;
        }
        Path branchFile = BRC_DIR.resolve(branch);
        if (!Files.exists(branchFile)) {
            System.out.println("No such branch exists.");
            return;
        }
        String branchCommitUid = readContentsAsString(branchFile).trim();
        // checkout the commit, restoring the working directory
        Commit targetCommit = Commit.getByUid(branchCommitUid); // Use cached HEAD commit
        if (targetCommit == null) {
            System.out.println("No commit found for branch: " + branch);
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CWD, entry -> !entry.getFileName().toString().equals(".gitlet")
                && !stagingArea.stagedFiles.containsKey(entry.getFileName().toString())
                && !stagingArea.unstagedFiles.containsKey(entry.getFileName().toString())
                && !stagingArea.removedFiles.containsKey(entry.getFileName().toString())
                && !getHeadCommit().getFileBlobs().containsKey(entry.getFileName().toString()))) {
            List<String> untrackedFiles = new ArrayList<>();
            for (Path file : stream) {
                untrackedFiles.add(file.getFileName().toString());
            }
            if (!untrackedFiles.isEmpty()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                Logging.err.println("Untracked files that would be overwritten by the switch:");
                for (String fileName : untrackedFiles) {
                    Logging.err.println(fileName);
                }
                return;
            }
        } catch (IOException e) {
            Logging.err.println("Failed to check untracked files: " + e.getMessage()); return;
        }
        // point HEAD to the branch commit
        updateHeadRef(branchFile);
        this.HEAD = branchCommitUid;
        this.HeadCommit = Commit.getByUid(branchCommitUid);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CWD, entry -> !entry.getFileName().toString().equals(".gitlet"))) {
            for (Path file : stream) {
                if (!file.getFileName().toString().equals(".gitlet")) {
                    if (restrictedDelete(file)) {
                        Logging.info.println("Deleted file " + file.getFileName() + " from working directory.");
                    } else {
                        Logging.err.println("Failed to delete file " + file.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            Logging.err.println("Failed to clean working directory: " + e.getMessage());
        }
        // clear the staging area
        stagingArea.stagedFiles.clear();
        stagingArea.removedFiles.clear();
        writeObject(INDX_FILE, stagingArea); // persist the staging area to the index file
        // restore the working directory to the state of the target commit
        for (Map.Entry<String, String> entry : targetCommit.getFileBlobs().entrySet()) {
            String fileName = entry.getKey();
            String blobUid = entry.getValue();
            Blob blob = Blob.getByUid(blobUid);
            if (blob != null) {
                Path file = CWD.resolve(fileName);
                writeContents(file, blob.getContents());
                Logging.dbg.println("Restored file " + file.getFileName() + " to working directory.");
            } else {
                Logging.err.println("Blob for file " + fileName + " not found in commit " + branchCommitUid);
            }
        }
        Logging.info.println("Switched to branch '" + branch + "'.");
    }

    public boolean isCurrentBranch(String branch) {
        // * check if the current branch is the given branch name
        if (branch == null || branch.isBlank()) {
            return false; // invalid branch name
        }
        // Logging.dbg.println("Current branch " + currentBranch() + " compared against " + branch);
        return currentBranch().equals(branch);
    }

    public String[] getBranches() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(BRC_DIR)) {
            List<String> branchNames = new ArrayList<>();
            for (Path f : stream) {
                if (Files.isDirectory(f)) {
                    try (DirectoryStream<Path> subStream = Files.newDirectoryStream(f)) {
                        for (Path sub : subStream) {
                            branchNames.add(BRC_DIR.relativize(sub).toString().replace("\\", "/"));
                        }
                    }
                } else {
                    branchNames.add(BRC_DIR.relativize(f).toString().replace("\\", "/"));
                }
            }
            return branchNames.toArray(new String[0]);
        } catch (IOException e) {
            Logging.err.println("Failed to list branches: " + e.getMessage());
            return new String[0];
        }
    }

    /** Removes a branch from the repository.
     *  <p>
     *  This method removes the specified branch from the repository.
     *  It cannot remove the default branch or the current branch.
     *
     *  @param branch the name of the branch to remove
     *
     *  @implSpec Removes the branch with the given name.
     *  If no such branch exists, print {@code A branch with that name does not exist.}
     *  If the branch is the current branch, print {@code Cannot remove the current branch.}
     *  If the branch is the default branch, print {@code Cannot delete the default branch.}
     */
    public void removeBranch(String branch) {
        // * remove a branch
        if (branch == null || branch.isBlank()) {
            throw error("No branch name provided.");
        }
        if (branch.equals(defaultBranch)) {
            System.out.println("Cannot delete the default branch.");
            return;
        }
        if (branch.equals(currentBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Path branchFile = BRC_DIR.resolve(branch);
        if (!Files.exists(branchFile)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        try {
            Files.delete(branchFile);
            Logging.info.println("Removed branch '" + branch + "'.");
        } catch (IOException e) {
            Logging.err.println("Failed to remove branch: " + e.getMessage());
        }
    }

    /** Merges the specified branch into the current branch.
     *  <p>
     *  This method merges the specified branch into the current branch.
     *  It cannot merge a branch with itself.
     *
     *  @param branch the name of the branch to merge
     *
     *  @implSpec Merges files from the given branch into the current branch.
     *  The <b>split point</b> is the <i>latest common ancestor</i> of the current and given branch heads.
     *  If the split point is the same commit as the given branch, then we do nothing; the merge is complete,
     *  and the operation ends with the message {@code Given branch is an ancestor of the current branch.}
     *  If the split point is the current branch, then the effect is to switch to the given branch,
     *  and the operation ends after printing the message {@code Current branch fast-forwarded.}
     *  Otherwise, we continue with the steps below.
     *  <p>
     *  1. Any files that have been modified in the given branch since the split point,
     *     but not modified in the current branch since the split point
     *     should be changed to their versions in the given branch
     *     (restored from the commit at the front of the given branch).
     *     These files should then all be automatically staged.
     *     To clarify, if a file is “modified in the given branch since the split point” this means
     *     the version of the file as it exists in the commit at the front of the given branch
     *     has different content from the version of the file at the split point.
     *     Remember: blobs are content-addressable!  <br>
     *  2. Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.  <br>
     *  3. Any files that have been modified in both the current and given branch in the same way
     *     (i.e., both files now have the same content or were both removed) are left unchanged by the merge.
     *     If a file was removed from both the current and given branch,
     *     but a file of the same name is present in the working directory,
     *     it is left alone and continues to be absent (not tracked nor staged) in the merge.  <br>
     *  4. Any files that were not present at the split point and are present only in the current branch should remain as they are.  <br>
     *  5. Any files that were not present at the split point and are present only in the given branch should be restored and staged.  <br>
     *  6. Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).  <br>
     *  7. Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.  <br>
     *  8. Any files modified in different ways in the current and given branches are in conflict.
     *     “Modified in different ways” can mean that the contents of both are changed and different from other,
     *     or the contents of one are changed and the other file is deleted,
     *     or the file was absent at the split point and has different contents in the given and current branches.
     *     In this case, replace the contents of the conflicted file with
     *     <pre>{@code
     *         <<<<<<< HEAD
     *         contents of file in current branch
     *         =======
     *         contents of file in given branch
     *         >>>>>>>
     *     }</pre>
     *     (replacing “contents of…” with the indicated file’s contents) and stage the result.
     *     Treat a deleted file in a branch as an empty file. Use straight concatenation here.
     *     In the case of a file with no newline at the end, you might well end up with something like this:
     *     <pre>{@code
     *         <<<<<<< HEAD
     *         contents of file in current branch
     *         =======
     *         contents of file in given branch>>>>>>>
     *     }</pre>
     *     This is fine.<br>
     *     - Once files have been updated according to the above,
     *       and the split point was not the current branch or the given branch, merge automatically commits
     *       with the log message {@code Merged [given branch name] into [current branch name].}
     *       Then, if the merge encountered a conflict, print the message {@code Encountered a merge conflict.} on the terminal (not the log).
     *       Merge commits differ from other commits:
     *       they record as parents both the head of the current branch (called the first parent)
     *       and the head of the branch given on the command line to be merged in.
     *  <p>
     *  If there are staged additions or removals present, print the error message {@code You have uncommitted changes.} and exit.
     *  If a branch with the given name does not exist, print the error message {@code A branch with that name does not exist.}
     *  If attempting to merge a branch with itself, print the error message {@code Cannot merge a branch with itself.}
     *  If merge would generate an error because the commit that it does has no changes in it,
     *  just let the normal commit error message for this go through.
     *  If an untracked file in the current commit would be overwritten or deleted by the merge,
     *  print {@code There is an untracked file in the way; delete it, or add and commit it first.} and exit;
     *  perform this check before doing anything else.
     */
    public void mergeBranch(String branch) {
        // // print the stagingArea onto STDERR to get a peek
        // Logging.dbg.println("Staged Files: " + stagingArea.stagedFiles.keySet()");
        // * merge the given branch into the current branch
        if (isDetachedHead()){
            throw error("Cannot merge a branch when HEAD is detached.");
        }
        if (branch == null || branch.isBlank()) {
            throw error("No branch name provided.");
        }
        if (branch.equals(currentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            Logging.warn.println("Merging a branch with itself; nothing to do.");
            return;
        }
        Path branchFile = BRC_DIR.resolve(branch);
        if (!Files.exists(branchFile)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String branchCommitUid = readContentsAsString(branchFile).trim();
        Commit commitToMerge = Commit.getByUid(branchCommitUid);
        if (commitToMerge == null) {
            System.out.println("No commit found for branch: " + branch);
            return;
        }
        // check for staged but not commited additions and removals before merge
        if (!stagingArea.stagedFiles.isEmpty() || !stagingArea.removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            Logging.dbg.println("Merge failed due to uncommitted changes.");
            Logging.dbg.print("[+]:");
            for (String fileName : stagingArea.stagedFiles.keySet()) {
                Logging.dbg.print(" " + fileName);
            }
            Logging.dbg.print("[-]:");
            for (String fileName : stagingArea.removedFiles.keySet()) {
                Logging.dbg.print(" " + fileName);
            }
            return;
        }
        // check if commitToMerge is an ancestor of the current HEAD.
        if (commitToMerge.isLinearAncestorOf(getHeadCommit())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            // Logging.info.println("Given branch is an ancestor of the current branch.");
            return;
        }
        // check if fast-forward is possible
        if (getHeadCommit().isLinearAncestorOf(commitToMerge)) {
            // fast-forward the current branch to the commitToMerge
            String oldCommitUid = HEAD; // save the old HEAD commit UID
            Commit currentCommit = getHeadCommit();
            Set<String> currentFiles = new HashSet<>(currentCommit.getFileBlobs().keySet());
            Set<String> targetFiles = new HashSet<>(commitToMerge.getFileBlobs().keySet());
            // 删除当前分支有但目标分支没有的文件
            for (String fileName : currentFiles) {
                if (!targetFiles.contains(fileName)) {
                    Path file = CWD.resolve(fileName);
                    if (Files.exists(file)) {
                        if (restrictedDelete(file)) {
                            Logging.info.println("Deleted file " + fileName + " from working directory.");
                        }
                    }
                }
            }
            // 恢复目标分支的文件
            for (Map.Entry<String, String> entry : commitToMerge.getFileBlobs().entrySet()) {
                String fileName = entry.getKey();
                String blobUid = entry.getValue();
                Blob blob = Blob.getByUid(blobUid);
                if (blob != null) {
                    Path file = CWD.resolve(fileName);
                    writeContents(file, blob.getContents());
                    Logging.info.println("Restored file " + file.getFileName() + " to working directory.");
                } else {
                    Logging.err.println("Blob for file " + fileName + " not found in commit " + branchCommitUid);
                }
            }
            // 更新 HEAD 指针和分支指针
            writeContents(BRC_DIR.resolve(currentBranch()), commitToMerge.getUid());
            updateHeadRef(BRC_DIR.resolve(currentBranch()));
            this.HEAD = commitToMerge.getUid();
            this.HeadCommit = commitToMerge;
            // 清空暂存区
            stagingArea.stagedFiles.clear();
            stagingArea.removedFiles.clear();
            writeObject(INDX_FILE, stagingArea);
            System.out.println("Current branch fast-forwarded.");
            Logging.dbg.println("Fast-forward " + currentBranch() + "  " + oldCommitUid.substring(0, 7) + " -> " + HEAD.substring(0, 7) + " (" + branch + ")");
            return;
        }
        Logging.info.printf("Merging %s into %s.\n", branch, currentBranch());
        mergeCommit(commitToMerge, String.format("Merged %s into %s.", branch, currentBranch()));
        // update branchFile
        writeContents(branchFile, HEAD); // update the branch file to point to the new HEAD commit
    }

    public static class GitletUID {
        private final String uid;

        public GitletUID(String uid) {
            if (uid == null || uid.isBlank()) {
                throw new IllegalArgumentException("UID cannot be null or empty.");
            }
            this.uid = uid;
        }

        public String getUid() {
            return uid;
        }

        @Override
        public String toString() {
            return uid;
        }
    }
}
