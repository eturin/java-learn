package ture;

import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        F<Integer> f = (a, b) -> a+b;

        var r = f.fx(1,2);
        System.out.println(r);

        Function<Integer,Integer> func = a -> a+1;
        System.out.println(func.apply(1));


    }
}