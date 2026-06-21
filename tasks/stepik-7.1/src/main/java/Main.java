import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Main {
    static void main() {
        List<Long> list = LongStream.range(0,1_000_000)
                .boxed()
                .collect(Collectors.toList());

        var spliterator1 = list.stream().spliterator();
        System.out.printf("1 - %d\n\n", spliterator1.estimateSize());

        var spliterator2 = spliterator1.trySplit();
        System.out.printf("1 - %d\n", spliterator1.estimateSize());
        System.out.printf("2 - %d\n\n", spliterator2.estimateSize());

        var spliterator3 = spliterator2.trySplit();
        System.out.printf("1 - %d\n", spliterator1.estimateSize());
        System.out.printf("2 - %d\n", spliterator2.estimateSize());
        System.out.printf("3 - %d\n\n", spliterator3.estimateSize());

        var spliterator4 = spliterator3.trySplit();
        System.out.printf("1 - %d\n", spliterator1.estimateSize());
        System.out.printf("2 - %d\n", spliterator2.estimateSize());
        System.out.printf("3 - %d\n", spliterator3.estimateSize());
        System.out.printf("4 - %d\n\n", spliterator4.estimateSize());

        var spliterator5 = spliterator4.trySplit();
        System.out.printf("1 - %d\n", spliterator1.estimateSize());
        System.out.printf("2 - %d\n", spliterator2.estimateSize());
        System.out.printf("3 - %d\n", spliterator3.estimateSize());
        System.out.printf("4 - %d\n", spliterator4.estimateSize());
        System.out.printf("5 - %d\n\n", spliterator5.estimateSize());

        var spliterator0 = spliterator1.trySplit();
        System.out.printf("0 - %d\n", spliterator0.estimateSize());
        System.out.printf("1 - %d\n", spliterator1.estimateSize());
        System.out.printf("2 - %d\n", spliterator2.estimateSize());
        System.out.printf("3 - %d\n", spliterator3.estimateSize());
        System.out.printf("4 - %d\n", spliterator4.estimateSize());
        System.out.printf("5 - %d\n\n", spliterator5.estimateSize());

    }
}
