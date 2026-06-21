package ture;

@FunctionalInterface
interface FF {
    A kkkk(int i, String s, double d);
}

class A {
    int k;
    String s;
    double d;
    A(int k, String s,double d) {
        this.k = k;
        this.s = s;
        this.d = d;
    }

    @Override
    public String toString() {
        return "A{" +
                "k=" + k +
                ", s='" + s + '\'' +
                ", d=" + d +
                '}';
    }
}
public class Main {
    public static void main(String[] args) {
        FF f = A::new;

        var a = f.kkkk(1, "hello", 1.3);
        System.out.println(a);
    }
}