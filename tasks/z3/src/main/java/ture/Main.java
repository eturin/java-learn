package ture;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;

/*
    Инвертирование букв и слов. Написать программу, которая инвертирует буквы
    каждого слова, и программу, которая инвертирует буквы каждого слова и
    сами слова.
 */
public class Main {
    public enum TYPE {
        WORDS, CHARS
    }

    public static String reverse(String s, TYPE type) {
        if (type == TYPE.WORDS) {
            var m = Arrays.asList(s.split(" "));
            Collections.reverse(m);
            return String.join(" ", m);
        }

        var list = new LinkedList<String>();
        for(var i = 0; i < s.length(); i++) {
            var cp = s.codePointAt(i);
            var ch = String.valueOf(Character.toChars(cp));
            if(ch.length() == 2) ++i;
            list.add(ch);
        }


        return String.join("", list.reversed());
    }

    public static String reverse2(String s, TYPE type) {
        if (type == TYPE.WORDS)
            return String.join(" ", Arrays.asList(s.split(" ")).reversed());


        return s.codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Collections.reverse(list);
                            return String.join("", list);
                        }
                ));
    }

    //графема - это видимый символ
    public static String reverse3(String s, TYPE type) {
        if (type == TYPE.WORDS)
            return String.join(" ", Arrays.asList(s.split(" ")).reversed());


        var graphemes = new LinkedList<String>();
        var matcher = java.util.regex.Pattern.compile("\\X").matcher(s);

        while (matcher.find()) {
            graphemes.add(matcher.group());
        }

        Collections.reverse(graphemes);
        return String.join("", graphemes);
    }
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNextLine()) {
            var line = scanner.nextLine();
            var r = reverse3(line, TYPE.WORDS);
            System.out.println(r);
            r = reverse3(line, TYPE.CHARS);
            System.out.println(r);
        }
    }
}