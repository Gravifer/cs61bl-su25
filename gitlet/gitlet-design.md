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

### Blob

> A **blob** object is nothing but a binary blob of data, and doesn't
refer to anything else.  There is no signature or any other verification
of the data, so while the object is consistent (it _is_ indexed by its
sha1 hash, so the data itself is certainly correct), it has absolutely
no other attributes.  No name associations, no permissions.  It is
purely a blob of data (ie normally "file contents").

#### Fields

1. Field 1
2. Field 2


### Commit

#### Fields

1. Field 1
2. Field 2


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
