package ture;


import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static Map<Character,Long> getRepeated(String s) {
        var map = new HashMap<Character, Long>();
        for(Character ch : s.toCharArray())
            map.put(ch, map.getOrDefault(ch, -1L) + 1);


        return map.entrySet()
                .stream()
                .filter(kv -> kv.getValue() > 0)
                .collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue()));
    }
    public static Map<Character,Long> getRepeated3(String s) {
        var set = new HashSet<Character>();
        var map = new HashMap<Character, Long>();
        for(Character ch : s.toCharArray())
            if(set.contains(ch)) map.put(ch, map.getOrDefault(ch, 0L) + 1);
            else                 set.add(ch);

        return map;
    }

    public static Map<Character,Long> getRepeated2(String s) {
        return s.chars()
                .mapToObj(ch -> (char) ch)  // Преобразуем int в Character
                .collect(Collectors.groupingBy(
                        ch -> ch,               // Группируем по символу
                        Collectors.collectingAndThen(
                                Collectors.counting(),   // Подсчитываем количество
                                count -> count - 1  // Вычитаем 1 после подсчёта
                        )
                ))
                .entrySet()
                .stream()
                .filter(kv -> kv.getValue() > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            var map = getRepeated3(line);

            for (var kv : map.entrySet()) {
                System.out.printf("%s: %d\n", kv.getKey(), kv.getValue());
            }
        }
    }
}