package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** The staging area, which is a map of file paths to their corresponding Blob UIDs.
 *  This is used to track files that are staged for commit.
 *  <p>
 *  Keys are file paths relative to the repository root,
 *  and values are fileInfo objects, containing some metadata and
 *  the SHA-1 hashes of the Blob objects representing the contents of those files.
 */
public class StagingArea implements Serializable { // StagingAria is mutable so it should not implement Dumpable

    /** A private class to hold file information.
     *  <p>
     *  Fields:
     *  - {@code filePath}: the path of the file relative to the repository root.
     *  - {@code blobUid}: the SHA-1 hash of the Blob object representing the contents of the file.
     *  - {@code ctime}: the creation time of the file, which is used to track when the file was added to the staging area.
     *  - {@code mtime}: the last modified time of the file, which is used to track when the file was last modified.
     *  - {@code size}: the size of the file in bytes, which is used to track the size of the file.
     */
    protected static class fileInfo implements Serializable {
        private String filePath;
        private String blobUid;
        private long ctime; // creation time
        private long mtime; // last modified time
        private long size;

        public fileInfo(String filePath, String blobUid, long ctime, long mtime, long size) {
            this.filePath = filePath;
            this.blobUid = blobUid;
            this.ctime = ctime;
            this.mtime = mtime;
            this.size = size;
        }
    }

    Map<String, fileInfo> stagedFiles;
    Map<String, fileInfo> unstagedFiles;
    Map<String, fileInfo> removedFiles;

    /** Initializes the staging area. */
    public StagingArea() {
        this.stagedFiles = new HashMap<>();
        this.unstagedFiles = new HashMap<>();
        this.removedFiles = new HashMap<>();
    }

    public Map<String, String> getStagedFileBlobs() {
        Map<String, String> stagedFileUids = new HashMap<>();
        for (Map.Entry<String, fileInfo> entry : stagedFiles.entrySet()) {
            stagedFileUids.put(entry.getKey(), entry.getValue().blobUid);
        }
        return stagedFileUids;
    }
    public Map<String, String> getUnstagedFileBlobs() {
        Map<String, String> unstagedFileUids = new HashMap<>();
        for (Map.Entry<String, fileInfo> entry : unstagedFiles.entrySet()) {
            unstagedFileUids.put(entry.getKey(), entry.getValue().blobUid);
        }
        return unstagedFileUids;
    }
    public Map<String, String> getRemovedFileBlobs() {
        Map<String, String> removedFileUids = new HashMap<>();
        for (Map.Entry<String, fileInfo> entry : removedFiles.entrySet()) {
            removedFileUids.put(entry.getKey(), entry.getValue().blobUid);
        }
        return removedFileUids;
    }
}
