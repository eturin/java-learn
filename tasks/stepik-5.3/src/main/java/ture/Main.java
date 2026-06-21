package ture;

import java.util.stream.Stream;

class Main {
    static void main() {
        var stream = Stream.of(1,2,3,4,5,6,7);

        stream.peek(System.out::println)
                .sorted()
                .filter(x -> x>5)
                .findFirst()
                .ifPresent(System.out::println);
    }
}