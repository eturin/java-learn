import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Application {
    public static void main(String[] args) {
        int[] m = {};
        var r = Arrays.stream(m)
                .boxed()
                .collect(Collectors.partitioningBy(x -> x > 10));
        System.out.println(r);
    }
}