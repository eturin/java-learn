package ture;

import java.security.KeyPair;
import java.util.*;
import java.util.stream.Collectors;

/*
Подсчет гласных и согласных. Написать программу, которая подсчитывает
число гласных и согласных букв в заданной строке. Сделать это для английского
языка, который имеет пять гласных (а, е, i, о и u).
 */
public class Main {
    public static Map<String, Long> count(String line) {
        var a = 0L;
        var b = 0L;
        var set = Set.of('a', 'e', 'i', 'o', 'u');
        for(var i = 0; i < line.length(); ++i) {
            var ch = Character.toLowerCase(line.charAt(i));
            if(Character.isAlphabetic(ch)) {
                if(set.contains(ch)) ++a;
                else ++b;
            }
        }

        return Map.of("Гласных", a, "Согласных", b);
    }

    public static Map<String, Long> count2(String line) {
        var set = Set.of('a', 'e', 'i', 'o', 'u','A', 'E', 'I', 'O', 'U');
        var r = line.chars()
                .filter(ch -> 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z')
                .mapToObj(ch -> (char) ch)
                .collect(Collectors.partitioningBy(set::contains, Collectors.counting()));

        return Map.of(
                "Гласных", r.getOrDefault(true, 0L),
                "Согласных", r.getOrDefault(false, 0L)
        );
    }

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNextLine()) {
            var line = scanner.nextLine();
            System.out.println(count(line));
        }

    }
}