package gitlet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ComparisonFailure;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;
import static org.junit.Assert.*;
import static com.google.common.truth.Truth.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitletAdditionalTests {
    static final Path SRC = Path.of("../test_files");
    static final Path WUG = SRC.resolve("wug.txt");
    static final Path NOTWUG = SRC.resolve("notwug.txt");
    static final Path WUG2 = SRC.resolve("wug2.txt");
    static final Path WUG3 = SRC.resolve("wug3.txt");
    static final Path CONFLICT1 = SRC.resolve("conflict1.txt");
    static final Path CONFLICT2 = SRC.resolve("conflict2.txt");
    static final Path CONFLICT3 = SRC.resolve("conflict3.txt");
    static final Path CONFLICT4 = SRC.resolve("conflict4.txt");
    static final Path CONFLICT5 = SRC.resolve("conflict5.txt");
    static final Path CONFLICT6 = SRC.resolve("conflict6.txt");
    static final Path A = SRC.resolve("a.txt");
    static final Path B = SRC.resolve("b.txt");
    static final Path C = SRC.resolve("c.txt");
    static final Path D = SRC.resolve("d.txt");
    static final Path E = SRC.resolve("e.txt");
    static final Path F = SRC.resolve("f.txt");
    static final Path G = SRC.resolve("g.txt");
    static final Path NOTA = SRC.resolve("nota.txt");
    static final Path NOTB = SRC.resolve("notb.txt");
    static final Path NOTF = SRC.resolve("notf.txt");
    static final String DATE = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d [-+]\\d\\d\\d\\d";
    static final String COMMIT_HEAD = "commit ([a-f0-9]+)[ \\t]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?" + DATE;
    static final String COMMIT_LOG = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE}[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))"
            .replace("${DATE}", DATE);
    static final String ARBLINE = "[^\\n]*(?=\\n|\\Z)";
    static final String ARBLINES = "(?:(?:.|\\n)*(?:\\n|\\Z)|\\A|\\Z)";

    private static final String COMMAND_BASE = "java gitlet.Main ";
    private static final int DELAY_MS = 150;
    private static final String TESTING_DIR = "testing";

    private static final PrintStream OG_OUT = System.out;
    private static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

    /**
     * Asserts that the test suite is being run in TESTING_DIR.
     * <p>
     * Gitlet does dangerous file operations, and is affected by the existence
     * of other files. Therefore, we must ensure that we are working in a known
     * directory that (hopefully) has no files.
     */
    public static void verifyWD() {
        Path wd = Path.of(System.getProperty("user.dir"));
        if (!wd.getFileName().endsWith(TESTING_DIR)) {
            fail("This test is not being run in the `testing` directory. " +
                    "Please see the spec for information on how to fix this.");
        }
    }

    @BeforeClass
    public static void setup01_verifyWD() {
        verifyWD();
    }

    /**
     * Asserts that no class uses nontrivial statics.
     * <p>
     * Using a JUnit tester over a multiple-execution script means that
     * we are running in a single invocation of the JVM, which means that
     * static variables keep their values. Rather than attempting to restore
     * static state (which is nontrivial), we simply ban any static state
     * aside from primitives, Strings (immutable), Paths (immutable),
     * Files (immutable), SimpleDateFormat (not immutable, but can't carry
     * useful info), and a couple utility classes for tests.
     * <p>
     * This test is not a game to be defeated. Even if you manage to smuggle
     * static state, the autograder will test your program by running it
     * over multiple invocations, and your static variables will be reset.
     *
     * @throws IOException
     */
    @BeforeClass
    public static void setup02_noNontrivialStatics() throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        for (String s : System.getProperty("java.class.path")
                .split(System.getProperty("path.separator"))) {
            if (s.endsWith(".jar")) continue;
            Path p = Path.of(s);
            Files.walkFileTree(p, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.toString().endsWith(".idea")) return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!file.toString().toLowerCase().endsWith(".class")) return FileVisitResult.CONTINUE;

                    String qualifiedName = p.relativize(file)
                            .toString()
                            .replace(File.separatorChar, '.');
                    qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 6);
                    try {
                        classes.add(Class.forName(qualifiedName));
                    } catch (ClassNotFoundException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        List<String> violations = new ArrayList<>();
        List<Class<?>> allowedClasses = List.of(
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                String.class,
                Path.class,
                File.class,
                SimpleDateFormat.class,
                // Utils
                FilenameFilter.class,
                // For testing stdout; not actually for use by students.
                ByteArrayOutputStream.class,
                PrintStream.class
        );
        for (Class<?> clazz : classes) {
            List<Field> staticFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()))
                    .toList();
            for (Field f : staticFields) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    violations.add("Non-final static field `" + f.getName() + "` found in " + clazz);
                }
                if (!allowedClasses.contains(f.getType())) {
                    violations.add("Static field `" + f.getName() + "` in " + clazz.getCanonicalName() +
                            " is of disallowed type " + f.getType().getSimpleName());
                }
            }
        }

        if (violations.size() > 0) {
            violations.forEach(OG_OUT::println);
            fail("Nontrivial static fields found, see class-level test output for GitletTests.\n" +
                    "These indicate that you might be trying to keep global state.");
        }
    }

    @BeforeClass
    public static void setup03_redirectStdout() {
        System.setOut(new PrintStream(OUT));
    }

    @BeforeClass
    @SuppressWarnings("removal")
    public static void setup04_trapSystemExit() {
        // https://openjdk.java.net/jeps/411
        // https://bugs.openjdk.java.net/browse/JDK-8199704
        // TODO: this is deprecated. See issues above.
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 0) {
                    throw new SecurityException("Allowable exit code, interrupting: " + status);
                }
            }

            // Default allow all - this isn't security sensitive
            @Override
            public void checkPermission(Permission perm) {
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
            }
        });
    }

    @AfterClass
    @SuppressWarnings("removal")
    public static void restoreSecurity() {
        // JUnit uses system.exit(0) internally somewhere, so hand control back
        // to the JVM before we leave the test class
        // TODO: this is deprecated. See the other call for relevant issue.
        System.setSecurityManager(null);
    }

    public void recursivelyCleanWD() throws IOException {
        // DANGEROUS: We're wiping the directory.
        // Must ensure that we're in the right directory, even though we did in setup01_verifyWD.
        verifyWD();

        // Recursively wipe the directory
        Files.walkFileTree(Path.of(System.getProperty("user.dir")), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                // Don't delete the directory itself, we're about to work in it!
                if (dir.toString().equals(System.getProperty("user.dir"))) {
                    return FileVisitResult.CONTINUE;
                }
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }

    @Before
    public void startWithEmptyWD() throws IOException, InterruptedException {
        recursivelyCleanWD();
        TimeUnit.MILLISECONDS.sleep(DELAY_MS);
    }

    @After
    public void endWithEmptyWD() throws IOException {
        recursivelyCleanWD();
    }

    /**
     * Returns captured output and flush the output stream
     */
    public static String getOutput() {
        String ret = OUT.toString();
        OUT.reset();
        return ret;
    }

    /**
     * Copies a source testing file into the current testing directory.
     *
     * @param src -- Path to source testing file
     * @param dst -- filename to write to; may exist
     */
    public static void writeFile(Path src, String dst) {
        try {
            OG_OUT.println("Copy source file " + src + " to testing file " + dst);
            Files.copy(src, Path.of(dst), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a file from the current testing directory.
     *
     * @param path -- filename to delete; must exist
     */
    public static void deleteFile(String path) {
        try {
            OG_OUT.println("Delete file " + path);
            Files.delete(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that a file exists in the current testing directory.
     *
     * @param path
     */
    public static void assertFileExists(String path) {
        OG_OUT.println("Check that file or directory " + path + " exists");
        if (!Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to exist; does not.");
        }
    }

    /**
     * Asserts that a file does not exist in the current testing directory.
     *
     * @param path
     */
    public static void assertFileDoesNotExist(String path) {
        OG_OUT.println("Check that file " + path + " does NOT exist");
        if (Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to not exist; does.");
        }
    }

    /**
     * Asserts that a file both exists in current testing directory and has
     * identical content to a source testing file.
     *
     * @param src        -- source testing file containin expected content
     * @param pathActual -- filename in current testing directory to check
     */
    public static void assertFileEquals(Path src, String pathActual) {
        OG_OUT.println("Check that source file " + src + " is identical to testing file " + pathActual);
        if (!Files.exists(Path.of(pathActual))) {
            fail("Expected file " + pathActual + " to exist; does not.");
        }
        try {
            String expected = Files.readString(src).replace("\r\n", "\n");
            String actual = Files.readString(Path.of(pathActual)).replace("\r\n", "\n");
            assertEquals("File contents of src file " + src + " and actual file " + pathActual + " are not equal",
                    expected, actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copied from Python testing script (`correctProgramOutput`). Intended to adjust for whitespace issues.
     * Removes trailing spaces on lines, and replaces multi-spaces with single spaces.
     *
     * @param s -- string to normalize
     * @return normalized output
     */
    public static String normalizeStdOut(String s) {
        return s.replace("\r\n", "\n")
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("(?m)^[ \\t]+", " ");
    }

    /**
     * Asserts that printed content to System.out is correct.
     *
     * @param expected -- expected printed content
     */
    public static void checkOutput(String expected) {
        expected = normalizeStdOut(expected).stripTrailing();
        String actual = normalizeStdOut(getOutput()).stripTrailing();
        assertEquals("ERROR (incorrect output)", expected, actual);
    }

    /**
     * Builds a command-line command from a provided arguments list
     *
     * @param args
     * @return command-line command, i.e. `java MyMain arg1 "arg with space"`
     */
    public static String createCommand(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (arg.contains(" ")) {
                sb.append('"').append(arg).append('"');
            } else {
                sb.append(arg);
            }
        }
        return sb.toString();
    }

    /**
     * Runs the given Gitlet command.
     *
     * @param args
     */
    public static void runGitletCommand(String[] args) {
        try {
            // Catch string ==
            for (int i = 0; i < args.length; i ++) {
                args[i] = new String(args[i]);
            }
            OG_OUT.println(COMMAND_BASE + createCommand(args));
            gitlet.Main.main(args);
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            // Wrap IOException and other checked for poor implementations;
            // can't directly catch it because it's checked and the compiler complains
            // that it's not thrown
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the given gitlet command and checks the exact output.
     *
     * @param args
     * @param expectedOutput
     */
    public static void gitletCommand(String[] args, String expectedOutput) {
        runGitletCommand(args);
        checkOutput(expectedOutput);
    }

    /**
     * Constructs a regex matcher against the output, for tests to extract groups.
     *
     * @param pattern
     * @return
     */
    public static Matcher checkOutputRegex(String pattern) {
        String actual = getOutput();
        pattern = normalizeStdOut(pattern).stripTrailing();
        String ogP = pattern;
        pattern += "\\Z";
        actual = normalizeStdOut(actual);
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(actual);
        if (!m.matches()) {
            m = p.matcher(actual.stripTrailing());
            if (!m.matches()) {
                // Manually raise a comparison error to get a rich diff for typo catching
                throw new ComparisonFailure("Pattern does not match the output",
                        ogP, actual.stripTrailing());
            }
        }
        return m;
    }

    /**
     * Runs the given gitlet command and checks that the output matches a provided regex
     *
     * @param args
     * @param pattern
     * @return Matcher from the pattern, for group extraction
     */
    public static Matcher gitletCommandP(String[] args, String pattern) {
        runGitletCommand(args);
        return checkOutputRegex(pattern);
    }

    public static void i_prelude1() {
        gitletCommand(new String[]{"init"}, "");
    }

    public static void i_setup1() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        writeFile(NOTWUG, "g.txt");
        gitletCommand(new String[]{"add", "g.txt"}, "");
        gitletCommand(new String[]{"add", "f.txt"}, "");
    }

    public static void i_setup2() {
        i_setup1();
        gitletCommand(new String[]{"commit", "Two files"}, "");
    }

    public static void i_blankStatus() {
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    public static void i_blankStatus2() {
        gitletCommand(new String[]{"status"}, """
                === Branches ===
                *main
                other

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    private static class IntrospectRepository extends Repository {
        public IntrospectRepository() {
            super();
        }

        private IntrospectRepository(StagingArea stagingArea) {
            super(stagingArea);
        }

        public StagingArea getStagingArea() {
            return stagingArea;
        }

        public static IntrospectRepository reinstantiate() {
            // * reinstantiate the repository from the current HEAD
            // * this method should read the HEAD file and restore the repository to that state
            IntrospectRepository repo;
            if (Files.exists(INDX_FILE)) {
                repo = new IntrospectRepository(readObject(INDX_FILE, StagingArea.class));
            } else {
                repo = new IntrospectRepository();
            }
            if (!Files.exists(HEAD_FILE)) {
                System.err.println("The HEAD file does not exist.");
                // throw error("fatal: not a gitlet repository (or any of the parent directories): .gitlet"); // mimic the behavior of git
                System.err.println("fatal: not a gitlet repository (or any of the parent directories): .gitlet");
                return null;
            }
            repo.HEAD = resolveHead();
            if (Files.exists(DESC_FILE)) {
                repo.description = readContentsAsString(DESC_FILE);
            }
            return repo;
        }
    }

    @Test
    public void myTest01_init_has_initialCommit() {
        gitletCommand(new String[]{"init"}, "");
        assertFileExists(".gitlet");
        assertFileExists(".gitlet/HEAD");
        // check whether the HEAD file contains the ref to the default branch
        assertWithMessage("HEAD file should contain the ref to the default branch")
                .that(readContentsAsString(Repository.HEAD_FILE))
                .isEqualTo("ref: refs/heads/main");
        // check whether the ref resolves to the UID of the latest commit
        assertWithMessage("HEAD file should contain the initial commit UID")
            .that(Repository.resolveHead())
            .isEqualTo(Commit.initialCommit().getUid());
        // assertEquals(Commit.initialCommit().getUid(), Files.readString(Path.of(".gitlet", "HEAD")));
    }

    @Test
    public void myTest02_add(){
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        // assert that number of files (recursive) under .gitlet/objects increased
        try (
            var beforeStream = Files.walk(Path.of(".gitlet/objects"))
        ) {
            long objectCountBefore = beforeStream.count();
            gitletCommand(new String[]{"add", "wug.txt"}, "");
            assertFileEquals(WUG, "wug.txt");
            assertFileExists(".gitlet/index");
            try (var afterStream = Files.walk(Path.of(".gitlet/objects"))) {
                long objectCountAfter = afterStream.count();
                assertTrue("the number of files under .gitlet/objects should increase", objectCountAfter > objectCountBefore);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to count files in .gitlet/objects", e);
        }
    }

    @Test
    public void myTest03_commit(){
        gitletCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        gitletCommand(new String[]{"add", "wug.txt"}, "");
        // assert that HEAD is updated and index is refreshed after commit
        String headOld= Repository.resolveHead();
        gitletCommand(new String[]{"commit", "added wug"}, "");
        IntrospectRepository repo = IntrospectRepository.reinstantiate();
        // check whether the HEAD file contains the ref to the default branch
        assertWithMessage("HEAD file should still contain the ref to the default branch")
            .that(readContentsAsString(Repository.HEAD_FILE))
            .isEqualTo("ref: refs/heads/main");
        // check whether the ref resolves to the UID of the latest commit
        String headNew = Repository.resolveHead();
        assertNotEquals("HEAD pointer is not updated", headOld, headNew);
        assertWithMessage("HEAD pointer is not updated to the UID of the latest commit").that(repo.getHead()).isEqualTo(headNew);
        // check whether the index is empty
        assertWithMessage("staged files should be cleared").that(repo.getStagingArea().stagedFiles).isEmpty();
    }
}
