package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 * <p>
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Gravifer
 */
public class Repository {
    /**
     * DONE: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The persisted store of serializations. */
    public static final File OBJ_DIR = join(GITLET_DIR, "objects");

    /** The hashes various refs point to. */
    public static final File REF_DIR = join(GITLET_DIR, "refs");

    /** The hashes various branches point to. */
    public static final File BRC_DIR = join(GITLET_DIR, "heads");

    /** The hashes various tags point to. */
    public static final File TAG_DIR = join(GITLET_DIR, "tags");

    /** The HEAD file saves the HEAD pointer UID */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    protected String HEAD;

    /** The description file saves the description of the repo */
    public static final File DESC_FILE = join(GITLET_DIR, "description");
    protected String description = "Unnamed repository; edit this file 'description' to name the repository.\n";

    /* TODO: fill in the rest of this class. */
}
