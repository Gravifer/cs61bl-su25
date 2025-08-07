package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;


/** Assorted utilities.
 *
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 *  @author P. N. Hilfinger
 */
public class Utils {

    /** The length of a complete SHA-1 UID as a hexadecimal numeral. */
    static final int UID_LENGTH = 40;

    /* SHA-1 HASH VALUES. */

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }
    static boolean restrictedDelete(Path filePath) {
        return restrictedDelete(filePath.toFile());
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp);
        }
    }
    static byte[] readContents(Path path) {
        return readContents(path.toFile());
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }
    static String readContentsAsString(Path path) {
        return readContentsAsString(path.toFile());
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp);
        }
    }
    static void writeContents(Path path, Object... contents) {
        writeContents(path.toFile(), contents);
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems. */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp);
        }
    }
    static <T extends Serializable> T readObject(Path path,
                                                 Class<T> expectedClass) {
        return readObject(path.toFile(), expectedClass);
    }

    /** Write OBJ to FILE. */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }
    static void writeObject(Path path, Serializable obj) {
        writeObject(path.toFile(), obj);
    }

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }
    static List<String> plainFilenamesIn(Path dir) {
        return plainFilenamesIn(dir.toFile());
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

    /** Return the concatenation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths get(String, String[])}
     *  method. */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /** Return the concatenation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths get(String, String[])}
     *  method. */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }
    static Path join(Path first, String... others){
        return Paths.get(first.toString(), others);
    }

    /* SERIALIZATION UTILITIES */

    /** Returns a byte array containing the serialized contents of OBJ. */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing ", obj);
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /** Return a GitletException whose message is composed of MSG and ARGS as
     *  for the String.format method. */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed of MSG and ARGS as for the String.format
     *  method, followed by a newline. */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    /**
     * Logging utility with color and stream methods.
     *
     * @see <a href="https://gist.github.com/JBlond/2fea43a3049b38287e5e9cefc87b2124">...</a>
     */
    public static class Logging {
        public static final Logger info = new Logger("INFO",    "\u001B[36m"); // Cyan
        public static final Logger dbg  = new Logger("DEBUG",   "\u001B[33m"); // Yellow
        public static final Logger warn = new Logger("WARNING", "\u001B[35m"); // Purple
        public static final Logger err  = new Logger("ERROR",   "\u001B[31m"); // Red

        public static class Logger {
            private final String level;
            private final String color;
            private final String reset = "\u001B[0m";
            public Logger(String level, String color) {
                this.level = level;
                this.color = color;
            }
            public void println(String msg) {
                System.err.println(color + msg + reset);
            }
            public void println() {
                System.err.println();
            }
            public void print(String msg) {
                System.err.print(color + msg + reset);
            }
            public void printf(String format, Object... args) {
                System.err.print(color + String.format(format, args) + reset);
            }
            public void printlnf(String format, Object... args) {
                System.err.println(color + "[" + level + "] " + String.format(format, args) + reset);
            }
            public void flush() {
                System.err.flush();
            }
            public void close() {
                // System.err cannot be closed, but method provided for API completeness
            }
            // You can extend with more methods: write, append, etc.
        }
    }

    public static void main(String[] args) {
        // Example usage of the Utils class
        File testFile = new File("test.txt");
        writeContents(testFile, "Hello, World!");
        System.out.println("Contents of test.txt: " + readContentsAsString(testFile));

        String hash = sha1("Hello", "World");
        System.out.println("SHA-1 hash: " + hash);

        // Clean up
        restrictedDelete(testFile);
    }
}
