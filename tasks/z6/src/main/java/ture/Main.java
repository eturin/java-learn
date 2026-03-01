package ture;

import java.util.Scanner;

/*
Подсчет появлений некоторого символа. Написать программу, которая
подсчитывает появления того или иного символа в заданной строке.
 */
public class Main {
    public static long charCount(String str, char ch) {
        long count = 0;
        for (int i = 0; i < str.length(); ++i) {
            count += str.charAt(i) == ch ? 1 : 0;
        }

        return count;
    }

    public static long charCount2(String str, char ch) {
        return str.chars()
                .filter(c -> c == ch)
                .count();
    }
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            if(scanner.hasNext()) {
                var ch = scanner.nextLine().charAt(0);
                System.out.println(charCount(line, ch));
            }
        }
    }
}