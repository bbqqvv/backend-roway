import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2";
        boolean match = encoder.matches("123123", hash);
        System.out.println("Match result: " + match);
        System.out.println("New hash for 123123: " + encoder.encode("123123"));
    }
}
