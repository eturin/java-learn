import java.util.function.Function;
import java.util.function.Predicate;

class ExtensionService {

    public static Function<String, String> addExtension(Predicate<String> p1, Predicate<String> p2) {
        return s -> p1.test(s) ? s+".xml" :
                          p2.test(s) ? s+".json" : s;
    }
}

public class Main {
    static void main() {

    }
}
