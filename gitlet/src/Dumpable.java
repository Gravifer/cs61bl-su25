package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/** An interface describing dumpable objects.
 *  @author P. N. Hilfinger
 */
interface Dumpable extends Serializable {
    /** Print useful information about this object on System.out. */
    void dump();
    /** Return a unique identifier for this object, which is a SHA-1 hash
     *  of its serialized form.
     *  The default implementation uses the sha1 method from Utils.
     *  @return a unique identifier for this object
     */
    default String getUid() {
        byte[] serialized = serialize(this);
        int length = serialized.length;
        return sha1(getDumpType(), "\0", serialize(length), "\0", serialize(this));
    }
    String getDumpType();

    static File persistFile(String uid) {
        // shard using the first two characters of the UID
        String shard = uid.substring(0, 2);
        String fileName = uid.substring(2);
        // create the directory structure
        File shardDir = join(Repository.OBJ_DIR, shard);
        File file = join(shardDir, fileName);
        // if already exists, return the file
        if (file.exists()) return file;

        // Create directories if they don't exist
        if (!shardDir.exists() && !shardDir.mkdirs()){
            throw new RuntimeException("Failed to create directory: " + shardDir.getAbsolutePath());
        }

        // Create the file if not existing
        try {
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create file: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
        }
        return file;
    }

    default void persist() {
        String uid = getUid();
        File file = persistFile(uid);

        // Open the file and write the object
        try {
            writeObject(file, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
        }
    }

    public static <T extends Serializable> T getByUid(String uid, Class<T> type) {
        // * get the commit from the object database
        File file = Dumpable.persistFile(uid);
        if (!file.exists()) {
            throw error("Object does not exist: " + uid);
        }
        return readObject(file, type);
    }
}
