package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;

import static gitlet.Utils.*;

public class Blob implements Serializable, Dumpable {
    /** Contents of this Blob, which is a byte array. */
    protected final byte[] contents;

    public Blob(byte[] contents) {
        this.contents = contents;
    }
    public Blob(String contents) {
        this.contents = contents.getBytes();
    }
    public Blob(Path filePath) {
        try {
            this.contents = java.nio.file.Files.readAllBytes(filePath);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
    public Blob(File file) {
        try {
            this.contents = java.nio.file.Files.readAllBytes(file.toPath());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read file: " + file.getName(), e);
        }
    }

    /** Create a Blob from a file, reading its contents and persisting it.
     *  This method reads the file's contents into a byte array, creates a Blob
     *  object, and persists it using the persist method.
     *  @param file the file to be converted into a Blob
     *  @return the created Blob object
     *  @serialData The contents of the file as a byte array, located in a shard in .gitlet/objects.
     */
    public static Blob blobify(File file) {
        try {
            byte[] fileContents = java.nio.file.Files.readAllBytes(file.toPath());
            Blob blob = new Blob(fileContents);
            blob.persist();
            return blob;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read file: " + file.getName(), e);
        }
    }
    public byte[] getContents() {
        return contents;
    }
    public String getContentsAsString() {
        return new String(contents);
    }

    /** Only compared by UID. Does <i>not</i> require the other object to be a Blob. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Dumpable other)) return false;
        return this.getUid().equals(other.getUid());
        // if (!(o instanceof Blob blob)) return false;
        // return uid.equals(Blob.uid);
    }

    /** Returns the hash code of this Blob, which is based on its UID.
     * This is used for storing blobs in hash-based collections like HashMap.
     * @return the hash code of this Blob
     */
    @Override
    public int hashCode() {
        return getUid().hashCode();
    }
    @Override
    public String toString() {
        return "Blob{" +
                "uid='" + getUid() + '\'' +
                ", contents='" + getContentsAsString() + '\'' +
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
        return "blob";
    }
}
