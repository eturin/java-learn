package ture;

import java.util.Scanner;

/*
Проверка, содержит ли строковое значение только цифры. Написать программу,
которая проверяет, что заданная строка содержит только цифры.
 */
public class Main {
    public static boolean check(String s) {
        return s.chars().allMatch(Character::isDigit);
    }
    public static boolean check2(String s) {
        for (var ch : s.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        return true;
    }


    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNextLine()) {
            var line = scanner.nextLine();
            System.out.println(check(line));
        }
    }
}