package ture;

import java.sql.Array;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        out: {
            var scanner = new Scanner(System.in);
            var m = new String[4];
            for(int i = 0; i < m.length; ++i) {
                if(scanner.hasNextLine()) m[i] = scanner.nextLine();
                else break out;
            }

            var r = String.format("%s|%s|%s|%s", m[0], m[1], m[3], m[2]);

            Supplier<String> f = r::toLowerCase;
            System.out.println(f.get());
        }

    }
}