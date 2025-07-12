# [Gitlet Design Document](https://cs61bl.org/su24/projects/gitlet/design)

  > GITLET	— an imbecile tangent follower
  > 
  > **gitlet** means a git diminutive.
  > 
  > This is an imbecile (and reasonably slow) directory tangent intern.  It 
  doesn't do a whole lot, but what it _does_ do is mimick efficient directory
  contents tracking.

**Name**: Gravifer

- This implementation loosely follows the structure of Git
  at [v0.99~954](https://github.com/git/git/tree/e83c5163316f89bfbde7d9ab23ca2e25604af290).
- For an example of a CS61B style design document,
  see [Capers](https://cs61bl.org/su24/projects/gitlet/capers-example).

## Classes and Data Structures

the provided `Dumpable` interface is augmented to require a `uid`,
via the following methods:

- `String getUid()` — returns the unique identifier of the object.
- `String getType()` — returns the type of the object, one of:
  - `blob` — a blob object.
  - `commit` — a commit object.
  - ~~`tree` — a tree object.~~\
      (as of now, the _repolet_ is flat, and trees are not distinguished from commits)
  - ~~`tag` — a tag object.~~
- `boolean verifyUid()` — verifies the `uid` of the object.
  - returns `true` if the `uid` is valid, `false` otherwise.

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

#### Fields

1. Field 1
2. Field 2


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

#### Fields

Commit instances should be immutable, so attributes are marked `final`. 
No need to make them `private`.
They're additionally marked `protected`, so that they can be accessed
by subclasses potentially defined elsewhere.

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

### Repository

#### Fields

1. Field 1
2. Field 2


### Tree

**As of now, the _repolet_ is flat.**


## Algorithms

## Persistence

The following classes are to be persisted in the _object database_,
aka _object store_, aka `SHA1_FILE_DIRECTORY`, 
aka `.dircache/`(in Git pre v0.99 terms), aka `.gitlet/`.

## Additional Tests
