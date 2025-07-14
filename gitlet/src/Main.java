package gitlet;

// @see https://stackoverflow.com/questions/2329358/is-there-anything-like-nets-notimplementedexception-in-java
// import org.apache.commons.lang3.NotImplementedException;  // * actually, don't use this in Java 11+ and above when there is no actual need to do so
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
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
    public static void main(String[] args) throws Exception {
        // DONE: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command."); // verbatim per spec
            return;
        }
        String firstArg = args[0];
        // the Repository class cannot stay static after all
        Repository repo;
        if (!firstArg.equals("init")) {
            repo = Repository.reinstantiate(); // 你需要实现 Repository.loadFromFile 方法
        } else {
            repo = new Repository();
        }

        // determine if this function is called from itself
        boolean calledFromSelf = StackWalker.getInstance().walk(frames ->
            frames.skip(1).findFirst().map(f ->
                f.getMethodName().equals("main") && f.getClassName().equals(Main.class.getName())).orElse(false));
        // boolean calledFromMain = getCallerClass() == Main.class;

        if (!calledFromSelf && (firstArg.equals("-i") || firstArg.equals("--interactive"))) {
            System.out.println("Entering interactive mode. Type 'exit' to exit.");
            interactiveMode(repo);
        } else if (firstArg.equals("-h") || firstArg.equals("--help")) {
            printHelp();
        } else {
            // handle commands
            cmd(repo, firstArg, Arrays.copyOfRange(args, 1, args.length));
        }
    }

    private static Repository cmd(Repository repo, String command, String[] args) throws IOException {
        UnsupportedOperationException todo = new UnsupportedOperationException(String.format("The '%s' command is not yet implemented.",command));
        switch(command) {
            case "init" -> {// * A Gitlet system is considered "initialized" in a particular location if it has a `.gitlet` directory there.
                Repository.init_db(); // DONE: handle the `init` command
                repo = Repository.reinstantiate();
            }
            case "add" -> {
                // DONE: handle the `add [filename]` command
                // check the filenames are valid
                if (args.length == 0) {
                    System.out.println("Please enter a file name to add.");
                    return repo;
                }
                // check if the file exists
                for (String filename : args) {
                    if (filename.isEmpty()) {
                        System.err.println("Empty file name. Ignored.");
                        continue;
                    }
                    File file = new File(filename);
                    if (!file.exists()) {
                        System.out.println("File does not exist.");
                        System.err.println("File does not exist: " + filename);
                        return repo;
                    }
                }
                for (String filename : args) {
                    repo.stageFile(filename);
                }
            }
            case "commit" -> {
                String message = args[0]; // other arguments are ignored
                repo.commit(message);
            }
            case "restore" -> {
                if (args[0].equals("--")) {
                    // restore files from HEAD
                    if (args.length < 2) {
                        System.out.println("Please specify a file to restore.");
                        return repo;
                    }
                    for (String filename : Arrays.copyOfRange(args, 1, args.length)) {
                        repo.restoreFile(filename);
                    }
                } else if (args.length > 1 && args[1].equals("--")) {
                    // restore files from specified commit
                    if (args.length < 3) {
                        System.out.println("Please specify a file to restore.");
                        return repo;
                    }
                    for (String filename : Arrays.copyOfRange(args, 2, args.length)) {
                        repo.restoreFile(args[0], filename);
                    }
                } else {
                    System.out.println("Invalid restore command. Use 'restore -- <file>' or 'restore <commit> -- <file>'.");
                    return repo;
                }
            }
            // DONE: FILL THE REST IN
            case "rm" -> {
                // remove files from the staging area
                // if the file is not staged, it will be removed from the working directory
                // if the file is staged, it will be removed from the staging area
                // if the file is not tracked, it will be ignored

                // check the filenames are valid
                if (args.length == 0) {
                    System.out.println("Nothing to remove.");
                    return repo;
                }
                // check if the file exists
                for (String filename : args) {
                    if (filename.isEmpty()) {
                        System.err.println("Empty file name. Ignored.");
                        continue;
                    }
                    File file = new File(filename);
                    if (!file.exists()) {
                        System.err.println("File does not exist: " + filename);
                    }
                }
                for (String filename : args) {
                    repo.removeFile(filename);
                }
            }
            case "log" ->
                repo.log();
            case "global-log" ->
                    throw todo;
            case "find" ->
                    throw todo;
            case "status" ->
                repo.status();
            case "branch" ->
                    throw todo;
            case "switch" ->
                    throw todo;
            case "rm-branch" ->
                    throw todo;
            case "reset" ->
                    throw todo;
            case "merge" ->
                    throw todo;
            default ->
                    System.out.println("No command with that name exists."); // * Per spec, this is the only output on System.out
            // throw new IllegalStateException("Unexpected value: " + firstArg);
        }
        return repo;
    }

    private static void interactiveMode(Repository repo) {
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
                String firstArg = inputArgs[0];
                if (firstArg.equals("exit")) {
                    System.out.println("Exiting Gitlet.");
                    return;
                }
                if (firstArg.equals("help")) {
                    printHelp();
                    continue;
                }
                inputArgs = Arrays.copyOfRange(inputArgs, 1, inputArgs.length);
                try {
                    repo = cmd(repo, firstArg, inputArgs);
                } catch (IOException e) {
                    System.err.println("An error occurred while executing the command: " + e.getMessage());
                } catch (UnsupportedOperationException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public static void printHelp() {
        System.out.println("Usage: java gitlet.Main <COMMAND> [<OPERAND1> <OPERAND2> ... | [OPTION]]");
        System.out.println("Commands:");
        System.out.println("  init       - Initialize a new Gitlet repository.");
        System.out.println("  add        - Add files to the staging area.");
        System.out.println("  commit     - Commit staged files.");
        System.out.println("  rm         - Remove files from the staging area.");
        System.out.println("  log        - Show the commit history.");
        System.out.println("  global-log - Show the global commit history.");
        System.out.println("  find       - Find commits by message.");
        System.out.println("  status     - Show the status of the repository.");
        System.out.println("  branch     - Create a new branch.");
        System.out.println("  checkout   - Switch branches or restore files.");
        System.out.println("  reset      - Reset the current branch to a specific commit.");
        System.out.println("  merge      - Merge another branch into the current branch.");
    }
}
