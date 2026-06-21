import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class NumberUtils {
    public static boolean isPrime(long n) {
        if(n < 2)        return false;
        else if (n == 2)  return true;
        else if (n%2 ==0) return false;
        else {
            long d = 3;
            while ( n >= d*d ){
                if (n%d == 0) return false;
                d += 2;
            }
        }

        return true;
    }
}

public class Main {
    public static LongStream createPrimesFilteringStream(long start, long end) {
        return LongStream.rangeClosed(start,end)
                .parallel()
                .filter(NumberUtils::isPrime);
    }
    public static void main(String[] args) {

        int result1 = List.of(1, 2, 3).parallelStream().reduce(100,  (a,x) -> a);
        System.out.println(result1); // 306

        var result2 = LongStream.range(0,100L)
                .parallel()
                .collect(Map.of("k",1L, "v", 0L),
                         Map<String,Long>a, long x) -> { a.put("v", a.get("v")+x); return a;});

        System.out.println(result2); // 515
        /*System.out.println(NumberUtils.isPrime(85));
        createPrimesFilteringStream(-10,100).forEach(System.out::println);*/
    }
}




