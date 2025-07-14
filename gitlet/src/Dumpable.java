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
        // validate the UID
        if (uid == null || uid.length() != 40 || !uid.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Invalid UID: " + uid);
        }
        // shard using the first two characters of the UID
        String shard = uid.substring(0, 2);
        String fileName = uid.substring(2);
        // create the directory structure
        File shardDir = join(Repository.OBJ_DIR, shard);
        File file = join(shardDir, fileName);
        // if already exists, return the file
        if (file.exists()) return file;

        // Create the shard directory if it doesn't exist; can do no harm
        if (!shardDir.exists() && !shardDir.mkdirs()){
            throw new RuntimeException("Failed to create directory: " + shardDir.getAbsolutePath());
        }

        // // Create the file if not existing // ! Don't, because this function may be passed an illegal UID
        // try {
        //     if (!file.createNewFile()) {
        //         throw new RuntimeException("Failed to create file: " + file.getAbsolutePath());
        //     }
        // } catch (Exception e) {
        //     throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
        // }
        return file;
    }

    default void persist() {
        String uid = getUid();
        File file = persistFile(uid);

        // * moved from persistFile to here
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Failed to create file: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
            }
        }
        // Open the file and write the object
        try {
            writeObject(file, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist object: " + e.getMessage(), e);
        }
    }

    public static <T extends Serializable> T getByUid(String uid, Class<T> type) {
        // * get the object from the object database; should be able to use any prefix no less than 7 characters
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        uid = resolveUid(uid);
        File file = Dumpable.persistFile(uid);
        if (!file.exists()) {
            throw error("Object does not exist: " + uid);
        }
        return readObject(file, type);
    }

    public static String resolveUid(String uid) {
        // * resolve the UID to a full UID if it is a prefix
        if (uid != null && uid.length() == 40 && uid.matches("[0-9a-fA-F]+")) {
            return uid; // already a full UID
        }
        if (uid == null || uid.length() < 7) {
            throw new IllegalArgumentException("Invalid UID prefix: " + uid);
        }
        File shardDir = join(Repository.OBJ_DIR, uid.substring(0, 2));
        // find the file matching the UID prefix, or else throw an error
        File[] files = shardDir.listFiles((dir, name) -> name.startsWith(uid.substring(2)));
        if (files == null || files.length == 0) {
            throw error("Failed to resolve UID: Object does not exist: " + uid);
        }
        if (files.length > 1) {
            throw error("Failed to resolve UID: Ambiguous UID prefix: " + uid);
        }
        return uid.substring(0, 2) + files[0].getName();
    }
}
