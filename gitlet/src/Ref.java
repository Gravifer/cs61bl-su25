package gitlet;

import java.io.Serializable;

/** A class representing a reference in Gitlet.
 *  This class is used to represent references to commits, branches, or tags. *
 */
public class Ref implements Serializable {
    protected final String name;
    protected final String uid;
    /** Creates a new Ref with the given name and UID.
     *  @param name the name of the reference (e.g., "master", "HEAD")
     *  @param uid the unique identifier for the reference (e.g., commit hash)
     */
    public Ref(String name, String uid) {
        this.name = name;
        this.uid = uid;
    }
}
