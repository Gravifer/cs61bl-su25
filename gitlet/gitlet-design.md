# [Gitlet Design Document](https://cs61bl.org/su24/projects/gitlet/design)

> GITLET — an imbecile tangent follower
> 
> **gitlet** means a git diminutive.
> 
> This is an imbecile (and reasonably slow) directory tangent intern.  It 
doesn't do a whole lot, but what it _does_ do is mimick efficient directory
contents tracking.

**Name**: Gravifer

- This implementation loosely follows the structure of Git
  at [v0.99~954](https://github.com/git/git/tree/e83c5163316f89bfbde7d9ab23ca2e25604af290).
  - Transparent persistence is implemented by serializing individual objects (blobs, commits, etc.) into a content-addressable object database (`.gitlet/objects`).
  - The repository state (staging area, HEAD, branches) is managed via files in `.gitlet/` and `.gitlet/refs/`.
- The design avoids global static state, but actually the abstraction is tempered to pass the autograder.
- For an example of a CS61B style design document,
  see [Capers](https://cs61bl.org/su24/projects/gitlet/capers-example).

## Classes and Data Structures

The provided `Dumpable` interface is augmented to require a `uid`,
via the following methods:

- `String getUid()` — returns the unique identifier of the object (SHA-1 hash).
- `String getDumpType()` — returns the type of the object, one of:
  - `blob` — a blob object.
  - `commit` — a commit object.
  - ~~`tree` — a tree object.~~\
      (as of now, the _repolet_ is flat, and trees are not distinguished from commits)
  - ~~`tag` — a tag object.~~
- `boolean verifyUid()` — verifies the `uid` of the object.
  - returns `true` if the `uid` is valid, `false` otherwise.
- `boolean verifyUid()` — verifies the `uid` of the object. *(not implemented, but can be added)*

### Blob

> A **blob** object is nothing but a binary blob of data, and doesn't
refer to anything else.  There is no signature or any other verification
of the data, so while the object is consistent (it _is_ indexed by its
sha1 hash, so the data itself is certainly correct), it has absolutely
no other attributes.  No name associations, no permissions.  It is
purely a blob of data (ie normally "file contents").

A blob can only correspond to a single file;
a file, on the other hand, might correspond to multiple blobs:
each being tracked in a different commit.

**Fields:**
- `byte[] contents` — the file contents.

**Key Methods:**
- Constructors for creating blobs from byte arrays, strings, files, or paths.
- `blobify` static method: creates and persists a blob from a file.
- `getUid()`: returns the SHA-1 hash of the blob's contents.
- `persist()`: saves the blob to the object database.

### Commit

> CHANGESET: The "changeset" object is an object that introduces the
notion of history into the picture.  In contrast to the other objects,
it doesn't just describe the physical state of a tree, it describes how
we got there, and why.
>
> A "changeset" is defined by the tree-object that it results in, the
parent changesets (zero, one or more) that led up to that point, and a
comment on what happened. Again, a changeset is not trusted per se:
the contents are well-defined and "safe" due to the cryptographically
strong signatures at all levels, but there is no reason to believe that
the tree is "good" or that the merge information makes sense. The
parents do not have to actually have any relationship with the result,
for example.

Commit instances should be immutable, so attributes are marked `final`.
No need to make them `private`.
They're additionally marked `protected`, so that they can be accessed
by subclasses potentially defined elsewhere.

**Fields:**
- `String[] parents` — UIDs of parent commits (supports merges).
- `Instant timestamp` — commit time.
- `String message` — commit message.
- `Map<String, String> fileBlobs` — mapping from filenames to blob UIDs.
- `boolean isEmpty` — whether the commit has no files.

<!--- 0. `String uid` — the commit message.
    - `== sha1("commit ",message, timestamp.toString(), authorTimestamp.toString(), Arrays.toString(parents))`
    - Getter: `getUid()`\
      Setter: `mkUid()` (private; only used during initialization)\
      Integrity checker: `verifyUid()` --->
1. **`parents`**: `String[]` — the parent commits UIDs.
2. **`timestamp`**: `Date` — the timestamp of the commit.
3. **`authorTimestamp`**: `Date` — the timestamp of the author;
    currently just an alias for the commit time.
4. **`message`**: `String` — the commit message.
5. **`fileBlobs`**: `Map<String, String>` — a map of file names to their blob UIDs.
    - The file name is the name of the file in the repository at the time of the commit.
    - The blob UID is the unique identifier of the blob object that contains the file contents.

UID is not stored as a field, but is computed on demand.

**Key Methods:**
- Constructors for normal and merge commits.
- `initialCommit()`: returns the singleton initial commit.
- `getUid()`: returns the SHA-1 hash of the commit's contents.
- `persist()`: saves the commit to the object database.
- `findLCA`: finds the latest common ancestor of two commits (for merges).
- `isLinearAncestorOf`: checks ancestry for fast-forward/merge logic.

### Repository

The **Repository** class manages the overall state of the version control system, including the working directory, object database, refs, branches, staging area, and HEAD pointer.
Note that the migration from `File` to `Path` has been made, but the design still uses `File` in some places for compatibility with the autograder.

**Fields:**
- `Path CWD` — current working directory.
- `Path GITLET_DIR` — `.gitlet` directory.
- `Path OBJ_DIR` — object database.
- `Path REF_DIR`, `BRC_DIR`, `TAG_DIR` — refs, branches, tags.
- `Path HEAD_FILE` — HEAD pointer file.
- `StagingArea stagingArea` — tracks staged/removed files.
- `String HEAD` — current HEAD commit UID.
- `String defaultBranch` — default branch name (usually `main`).
- `HashSet<String> allCommitUids` — tracks all commit UIDs for global log/find.

**Key Methods:**
- `init_db()`: initializes the repository and object database.
- `reinstantiate()`: reloads repository state from disk.
- `stageFile()`: stages a file for commit.
- `commit()`: creates a new commit from staged/removed files.
- `restoreFile()`: restores a file from a commit to the working directory.
- `resetHardCommit()`: resets the working directory and branch to a specific commit.
- `checkoutBranch()`, `switchBranch()`: switch branches and update working directory.
- `removeFile()`: removes files from staging/tracking.
- `log()`, `global-log()`, `find()`: history and search commands.
- `mergeBranch()`: merges another branch into the current branch, handling conflicts.

### StagingArea

The **StagingArea** class tracks files staged for addition or removal before committing. It is serialized to `.gitlet/index`.

**Fields:**
- `Map<String, fileInfo> stagedFiles` — files staged for addition.
- `Map<String, fileInfo> removedFiles` — files staged for removal.
- `Map<String, fileInfo> unstagedFiles` — (not used, but present for completeness).

**Key Methods:**
- `getStagedFileBlobs()`, `getRemovedFileBlobs()`: return mappings for commit construction.

### Tree

**As of now, the _repolet_ is flat.**

### Ref

The **Ref** class represents a reference to a commit (supposed to be used for branches/tags but for now doesn't actually do anything).

**Fields:**
- `String name` — name of the ref.
- `String uid` — commit UID.

## Algorithms

- **Persistence:** All objects (blobs, commits) are serialized and stored in `.gitlet/objects/` using SHA-1 as the filename (sharded by first two hex digits).
- **Commit History:** Commit history is traversed by following parent links; merge commits have multiple parents.
- **Merge:** Merges use split point (LCA) detection and follow the spec's rules for file resolution and conflict handling.
- **Branching:** Branches are files in `.gitlet/refs/heads/` pointing to commit UIDs. HEAD is a ref to a branch or a commit (detached).
- **Status:** Status is computed by comparing the working directory, staging area, and HEAD commit.

## Persistence

- **Object Database:**,
  aka _object store_, aka `SHA1_FILE_DIRECTORY`,
  aka `.dircache/`(in Git pre v0.99 terms), aka `.gitlet/`.
  - Blobs and commits are persisted in `.gitlet/objects/`.
  - Staging area is persisted in `.gitlet/index`.
  - HEAD and branch pointers are files in `.gitlet/HEAD` and `.gitlet/refs/heads/`.
  - Description is in `.gitlet/description`.

## Additional Tests

- The implementation passes the provided autograder and additional tests in `GitletAdditionalTests.java`.
- All commands are tested for correctness, error handling, and edge cases (e.g., branch switching, merge conflicts, fast-forward, status output).
- No nontrivial static state is used, as required by the autograder.

## Notes

- The design is flat (no tree objects), and tags are not implemented.
- All file operations are performed relative to the working directory.
- Error messages and outputs follow the spec verbatim.
- Logging and debug output are sent to stderr, not stdout.

- **Some other implementations to check out**
  - https://github.com/Abdelrhmansersawy/Gitlet
  - https://github.com/annetta-zheng/Gitlet
  - https://github.com/hrishikeshh/Gitlet
  - https://github.com/jacobakh/Gitlet
  - https://github.com/pichardo13/gitlet
