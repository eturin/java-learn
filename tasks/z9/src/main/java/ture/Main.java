package ture;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

/*
Соединение нескольких строк с помощью разделителя. Написать программу,
которая соединяет заданные строки с помощью заданного разделителя.
 */
public class Main {
    static String join(String delimiter, String[] m) {
        return String.join(delimiter, m);
    }
    public static void main(String[] args) {
        out: {
            var scanner = new Scanner(System.in);

            if(!scanner.hasNext()) break out;
            var tr = scanner.next();
            var list = new LinkedList<String>();
            while (scanner.hasNextLine())
                list.add(scanner.nextLine());


            System.out.println(join(tr,list.toArray(new String[list.size()])));
        }
    }
}