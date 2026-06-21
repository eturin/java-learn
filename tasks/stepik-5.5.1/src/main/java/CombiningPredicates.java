import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CombiningPredicates {

    /**
     * The method represents the conjunction operator for a stream of predicates.
     * For an empty stream it returns the always true predicate.
     */
    public static IntPredicate conjunctAll(Stream<IntPredicate> predicates) {
        //return predicates.reduce((a, b) -> a.and(b)).orElse(x -> true);
        return predicates.reduce(x -> true, (a, b) -> a.and(b));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] strings = scanner.nextLine().split("\\s+");

        List<Boolean> values = Arrays.stream(strings)
                .map(CombiningPredicates::parseBooleanOrTrue)
                .collect(Collectors.toList());

        List<IntPredicate> predicates = new ArrayList<>();
        values.forEach(v -> predicates.add(x -> v));

        System.out.println(conjunctAll(predicates.stream()).test(0));
    }

    private static boolean parseBooleanOrTrue(String item) {
        if (item == null || "".equals(item.trim())) {
            return true;
        }
        return Boolean.parseBoolean(item);
    }
}