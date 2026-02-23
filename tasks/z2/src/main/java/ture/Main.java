package ture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
  Отыскание первого неповторяющегося символа. Написать программу, которая
  возвращает первый неповторяющийся (некратный) символ в заданной строке.
*/
public class Main {
    public static String getFirstUniqChar(String s) {
        if (s == null || s.length() == 0) return null;

        var map = new HashMap<String, Boolean>();
        var list = new ArrayList<String>();
        for (var i = 0; i < s.length(); ++i) {
            var cp = s.codePointAt(i);
            var ch = String.valueOf(Character.toChars(cp));
            if(ch.length() == 2) ++i;
            map.put(ch, !map.containsKey(ch));
            list.add(ch);
        }

        for(var ch : list)
            if(map.get(ch))
                return ch;

        return null;
    }

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNextLine()) {
            var line = scanner.nextLine();
            var ch = getFirstUniqChar(line);
            System.out.println(ch);
        }
    }
}