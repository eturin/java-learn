package ture;

import java.util.Scanner;

/*
Конвертирование строки в значение типа int, long, float или double. Написать
программу, которая конвертирует заданный объект типа string (представляющий
 число) в значение типа int, long, float или double.
 */
public class Main {
    record Numbers(Integer n, Long l, Float f, Double d) {};

    static Numbers toNumbers(String str) {
        var s = str.trim();
        Integer n = null;
        try { n = Integer.valueOf(s);   } catch (NumberFormatException e) {  }
        Long l = null;
        try { l = Long.valueOf(s);     } catch (NumberFormatException e) {  }
        Float f = null;
        try { f = Float.valueOf(s);   } catch (NumberFormatException e) {  }
        Double d = null;
        try { d = Double.valueOf(s); } catch (NumberFormatException e) {  }

        return new Numbers(n, l, f, d);
    }

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        if(scanner.hasNext()) {
            var s = scanner.next();
            System.out.println(toNumbers(s));
        }
    }
}