package ture;

public class Main {
    public static void main(String[] args) {
        var o = new F() {
            boolean b = false;

            @Override
            public int apply(String s) {
                b = !b;
                System.out.printf("[%b] %s\n", b,s);
                return help(s);
            }

            private int help(String s) {
                return s.length();
            }
        };

        System.out.printf("len = %d\n", o.apply("Hello"));
        System.out.printf("len = %d\n", o.apply("You"));
    }
}