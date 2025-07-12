package gitlet;

// @see https://stackoverflow.com/questions/2329358/is-there-anything-like-nets-notimplementedexception-in-java
// import org.apache.commons.lang3.NotImplementedException;  // * actually, don't use this in Java 11+ and above when there is no actual need to do so
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

// import static jdk.internal.reflect.Reflection.getCallerClass; // ! Package 'jdk.internal.reflect' is declared in module 'java.base', but the latter does not export it to the unnamed module.
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
 *  @author Gravifer
 *  @version 0.1
 *  @since 2025-07-10
 *  @see <a href="https://cs61bl.org/su24/projects/gitlet/#overall-spec">CS 61BL Gitlet Project (su24)</a>
 */
public class Main {
    // private static boolean interactiveMode = false; // non-final static fields disallowed in this project

    /// Usage: `java gitlet.Main ARGS`,\
    /// where `ARGS` contains
    /// `(<COMMAND> <OPERAND1> <OPERAND2> ... | [OPTION])`
    ///
    /// `COMMAND` is one of the following:\
    /// `init` `add` `commit` `rm` `log` `global-log` `find` `status`
    /// `branch` `checkout` `reset` `merge`.
    ///
    /// Options (only one can be supplied at a time):
    /// - `-h` `--help`        - print help message.
    /// - `-i` `--interactive` - run commands interactively.\
    ///     In interactive mode, the program will prompt for commands until `exit` is entered.
    /// <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
    public static void main(String[] args) {
        // DONE: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command."); // verbatim per spec
            return;
        }
        String firstArg = args[0];

        // determine if this function is called from itself
        boolean calledFromSelf = StackWalker.getInstance().walk(frames ->
            frames.skip(1).findFirst().map(f ->
                f.getMethodName().equals("main") && f.getClassName().equals(Main.class.getName())).orElse(false));
        // boolean calledFromMain = getCallerClass() == Main.class;

        if (!calledFromSelf && (firstArg.equals("-i") || firstArg.equals("--interactive"))) {
            // interactiveMode = true;
            while (true) {
                // use commands interactively
                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Supply a command or exit: [init, add, commit, rm, log, global-log, find, status, branch, checkout, reset, merge]");
                if (myObj.hasNextLine()) {
                    String input = myObj.nextLine();
                    String[] inputArgs = input.split(" ");
                    if (inputArgs.length == 0) {
                        System.out.println("Please enter a command.");
                        continue;
                    }
                    firstArg = inputArgs[0];
                    if (firstArg.equals("exit")) {
                        System.out.println("Exiting Gitlet.");
                        // interactiveMode = false;
                        return;
                    }
                    // handleCommand(firstArg, inputArgs);
                    main(new String[]{firstArg});
                }
            }
        }

        switch(firstArg) {
            case "init" -> // * A Gitlet system is considered "initialized" in a particular location if it has a `.gitlet` directory there.
                // DONE: handle the `init` command
                init_db();
            case "add" ->
                // TODO: handle the `add [filename]` command
                throw new UnsupportedOperationException("The 'add' command is not yet implemented.");
            // TODO: FILL THE REST IN
            default ->
                System.out.println("No command with that name exists.");
                // throw new IllegalStateException("Unexpected value: " + firstArg);
        }
    }

    /** Initializes the {@code .gitlet} directory, aka the Gitlet database.
     * <p>
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit:
     * a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
     * It will have a single branch: main, which initially points to this initial commit, and main will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
     * in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0).
     * Since the initial commit in all repositories created by Gitlet will have exactly the same content,
     * it follows that all repositories will automatically share this commit (they will all have the same UID)
     * and all commits in all repositories will trace back to it.
     *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
     *
     * @implNote for now, it is not clear if repo initialization should be moved to another class
     */
    private static void init_db() {
        // * the provided Repository class uses CWD as the repo root naively,
        // * so we should always do verifications in Main
        if (Repository.GITLET_DIR.exists()) {
            // * the spec does not ask for reinitialization of the repository, so we should not allow it
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        String dbPath = Repository.GITLET_DIR.getAbsolutePath();
        try {
            if (Repository.GITLET_DIR.mkdir()) {
                // if the directory was created successfully, we can initialize the repository
                // TODO: create the initial commit
                // TODO: create the default branch ("master")
                Commit initialCommit = Commit.initialCommit(); // initial commit is empty and a singleton, so no need to persist it
                // nevertheless, we should initialize the object database
                if (!Repository.OBJ_DIR.exists() && !Repository.OBJ_DIR.mkdirs()) {
                    throw error("Unable to create objects directory in .gitlet/ directory.");
                }
                if (!Repository.OBJ_DIR.isDirectory()) {
                    throw error("The objects directory in .gitlet/ is not a directory.");
                }

                if (!Repository.HEAD_FILE.createNewFile() && !Repository.HEAD_FILE.createNewFile()) { // create the HEAD file
                    throw error("Unable to create HEAD file in .gitlet/ directory.");
                }
                writeContents(Repository.HEAD_FILE, initialCommit.uid); // write the initial commit UID to HEAD
                System.err.println("Initialized an empty Gitlet repository in " + dbPath); // Per spec, no output on System.out
            } else {
                System.err.printf("init_db(): java.io.File.mkdir() returned false for " + dbPath);
                // System.exit(1); // ! Per spec, always exit with exit code 0, even in the presence of errors.
                throw new IOException(String.format("Unable to create directory at " + dbPath));
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred while initializing the Gitlet repository: " + e.getMessage());
            throw new GitletException("Unable to create .gitlet/ directory.");
        }
    }
}
