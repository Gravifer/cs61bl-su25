import java.util.Arrays;
import java.util.Objects;

/** Represents an example of how a website might model a user. */
public class User implements Comparable<User>{

    /* DONE: Make Users a comparable type.
            Use parameterization (i.e., <>) to ensure that User can only be
            used to compare against other Users. */
    private static int nextId = 1;

    private static final int AGE_MODULUS = 13;
    private static final int AGE_CONSTANT = 20;

    private int id;
    private int age;
    private String name;
    private String email;

    public User(String name, String email) {
        this(nextId, name, email);
        nextId += 1;
    }

    /** Force assign an id to a created user **/
    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        setAge();
    }

    /** For this assignment, age is just an automatically assigned field. */
    void setAge() {
        age = (id % AGE_MODULUS) + AGE_CONSTANT;
    }

    int getAge() {
        return age;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name=" + name + ", email=" + email + "}";
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(User o) {
        if (this == o) {
            return 0;
        } else if (o == null) {
            throw new ClassCastException("A User cannot be compared to a " + o.getClass().getName());
        }

        User other = o;
        if (id != other.id) {
            return id - other.id;
        } else { // if (!Objects.equals(name, other.name)) {
            return name.compareTo(other.name);
        } // else {
        //     return email.compareTo(other.email);
        // }
    }

    /** Returns whether or not two Users are considered equal to each other. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User other = (User) o;
        if (id != other.id) {
            return false;
        } else if (!Objects.equals(name, other.name)) {
            return false;
        } else {
            return Objects.equals(email, other.email);
        }
    }

    public static void main(String[] args) {
        User[] users = {
                new User(2, "Noah", ""),
                new User(4, "Wilson", ""),
                new User(5, "Karen", ""),
                new User(1, "Yinqi", ""),
                new User(1, "Amy", "")
        };
        Arrays.sort(users);
        for (User user : users) {
            System.out.println(user);
        }
    }
}
