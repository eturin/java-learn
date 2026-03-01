package ture;

/*
Удаление пробелов из строки. Написать программу, которая удаляет все
пробелы из заданной строки.
 */

import java.util.Scanner;

public class Main {
    public static String clean(String s){
        return s.replace(" ","");
    }

    public static String clean2(String s){
        var sb = new StringBuilder();
        for(var i = 0; i < s.length(); ++i){
            var ch = s.charAt(i);
            if(ch != ' ')
                sb.append(ch);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNextLine()) {
            var line = scanner.nextLine();
            System.out.println(clean2(line));
        }
    }
}