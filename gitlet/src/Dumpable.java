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

    default void persist() {
        String uid = getUid();
        // shard using the first two characters of the UID
        String shard = uid.substring(0, 2);
        String fileName = uid.substring(2);
        // create the directory structure
        File shardDir = join(Repository.OBJ_DIR, shard);
        File file = join(shardDir, fileName);
        // if already exists, do nothing, since the UID is unlikely to clash
        // If the file already exists, do nothing
        if (file.exists()) return;

        // Create directories if they don't exist
        if (!shardDir.exists() && !shardDir.mkdirs()){
            throw new RuntimeException("Failed to create directory: " + shardDir.getAbsolutePath());
        }

        // Create file and write object
        try {
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create file: " + file.getAbsolutePath());
            }
            writeObject(file, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
        }
    }
}
