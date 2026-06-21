import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InflationProblem {

    public static long calculateTotalPriceInFuture(int numberOfYears, List<Grocery> groceries) {
        var map = Map.of(Category.VEGETABLES, 3, Category.FRUITS, 4, Category.DAIRY, 2);

        return groceries.stream()
                .map(x -> x.getCost() * (long) Math.pow(map.get(x.getCategory()), numberOfYears))
                .reduce(Long::sum)
                .orElse(0L);
        /*return groceries.stream()
                .collect(Collectors.reducing(0L,
                                             (Grocery x) -> x.getCost() * (long) Math.pow(map.get(x.getCategory()), numberOfYears),
                                             Long::sum));*/
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int numberOfYears = Integer.parseInt(scanner.nextLine());

        List<Grocery> groceries = Stream.iterate(1, i -> scanner.hasNextLine(), i -> i + 1)
                .map(i -> scanner.nextLine())
                .map(inputLine -> {
                    String[] parts = inputLine.split("\\s+");
                    return new Grocery(Long.parseLong(parts[0]), Category.valueOf(parts[1]));
                })
                .collect(Collectors.toList());

        long totalPriceInFuture = calculateTotalPriceInFuture(numberOfYears, groceries);

        System.out.println(totalPriceInFuture);
    }
}

enum Category {
    VEGETABLES,
    FRUITS,
    DAIRY
}

class Grocery {
    private final long cost;
    private final Category category;

    // Imagine that this class has some other fields but they are skipped for simplicity

    public Grocery(long cost, Category category) {
        this.cost = cost;
        this.category = category;
    }

    public long getCost() {
        return cost;
    }

    public Category getCategory() {
        return category;
    }
}