package gitlet;

// @see https://stackoverflow.com/questions/2329358/is-there-anything-like-nets-notimplementedexception-in-java
// import org.apache.commons.lang3.NotImplementedException;  // * actually, don't use this in Java 11+ and above when there is no actual need to do so

import java.io.IOException;
import java.util.Scanner;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
 *  @author Gravifer
 *  @version 0.1
 *  @since 2025-07-10
 *  @see <a href="https://cs61bl.org/su24/projects/gitlet/#overall-spec">CS 61BL Gitlet Project (su24)</a>
 */
public class Main {

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

        if (!calledFromSelf && (firstArg.equals("-i") || firstArg.equals("--interactive"))) {
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
                        return;
                    }
                    // handleCommand(firstArg, inputArgs);
                    main(new String[]{firstArg});
                }
            }
        }

        switch(firstArg) {
            case "init" -> {
                // TODO: handle the `init` command
                init_db();
            }
            case "add" -> {
                // TODO: handle the `add [filename]` command
                throw new UnsupportedOperationException("The 'add' command is not yet implemented.");
            }
            // TODO: FILL THE REST IN
            default -> {
                throw new IllegalStateException("Unexpected value: " + firstArg);
            }
        }
    }

    /** Initializes the {@code .gitlet} directory, aka the Gitlet database.
     *
     *  <header><blockquote>GITLET - A tattling intern from heck</blockquote></header>
     *
     * @implNote for now it is not clear if repo initialization should be moved to a separate class
     */
    private static void init_db() {
        // the provided Repository class uses CWD as the repo root naively,
        // so we should always do verifications in Main
        if (Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        try {
            Repository.GITLET_DIR.mkdir();
            System.out.println("Initialized an empty Gitlet repository in " + Repository.GITLET_DIR.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Unable to create .gitlet/ directory.");
            System.out.println("An unexpected error occurred while initializing the Gitlet repository: " + e.getMessage());
        }
    }
}
