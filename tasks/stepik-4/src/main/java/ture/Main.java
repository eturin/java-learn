package ture;

import java.util.Optional;

class A {
    private final String name;
    A(String name) {
        this.name = name;
    }

    String getName() { return name;}
    @Override
    public String toString() {
        return "A{" +
                "name='" + name + '\'' +
                '}';
    }
}

class B {
    private final A a;
    B(A a) {
        this.a = a;
    }

    Optional<A> getA() {
        return Optional.ofNullable(a);
    }

    @Override
    public String toString() {
        return "B{" +
                "a=" + a +
                '}';
    }
}

public class Main {
    static void main() {
        var a = new A("hi");
        var b = new B(null);
        System.out.println(a);
        System.out.println("\nnew B(null);");
        System.out.println(b);
        System.out.println(b.getA());

        System.out.println("\nnew B(new A(\"hi\"));");
        b = new B(a);
        System.out.println(b);

        System.out.println("\nx");
        var v1 = b.getA();
        System.out.println(v1);
        var v2 = v1.map(x -> x.getName());
        System.out.println("\nmap");
        System.out.println(v2);

        System.out.println("\nmap");
        var v3 = v2.map( x -> x.length());
        System.out.println(v3);


        var v4 = Optional.of(Optional.of("hello"));
        System.out.println("\nOptional.of(Optional.of(\"hello\"))");
        System.out.println(v4);

        System.out.println("\nmap");
        var v5 = v4.map( x -> x.toString());
        System.out.println(v5);

        System.out.println("\nflatMap");
        var v6 = v4.flatMap( x -> Optional.of(x.toString()));
        System.out.println(v6);


        Optional<Integer> v7 = Optional.ofNullable(1); //Optional.empty();
        System.out.println("\nmap");
        var v8 = v7.map( x -> Optional.of(x+1));
        System.out.println(v8);

        System.out.println("\nflatMap");
        var v9 = v7.flatMap( x -> Optional.of(x+1));
        System.out.println(v9);
    }
}
