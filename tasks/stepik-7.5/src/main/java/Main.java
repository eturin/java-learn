import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class QueryUtils {

    public static <R> Map<String, R> execute(List<String> tables, Function<String, R> query, R defaultValue) {
        Map<String, R> tableToResultMap = new ConcurrentHashMap<>();

        CompletableFuture<?>[] futures = tables.stream()
                .map(s -> CompletableFuture.supplyAsync(() -> query.apply(s))
                                                 .handle((result, throwable) -> tableToResultMap.put(s, throwable != null ? defaultValue : result)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        return tableToResultMap;
    }
}

public class Main {
    static void main() {

    }
}
