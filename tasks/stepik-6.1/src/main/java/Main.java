import java.util.stream.Stream;

final class Utils {

    private Utils() { }

    public static Stream<User> generateUsers(int numberOfUsers) {
        return Stream.iterate(0, i -> i < numberOfUsers, i -> i+1)
                .map(i -> new User(i, String.format("u%d@bla.bla", i)));
    }
}

class User {
    private final long id;
    private final String email;

    User(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String toString() {
        return String.format("User[id=%d, email=%s]", id, email);
    }


}

public class Main {
    static void main() {
        var stream = Utils.generateUsers(10);
        stream.forEach(System.out::println);
    }
}
