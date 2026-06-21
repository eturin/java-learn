package ture;

class Main {
    public static void main(String[] args) {
        var a = new A<Integer>(10);
        var b = new A<String>("Hello");

        System.out.printf("a.x = %d\nb.x = %s\n(a.x.T == b.x.T) = %s", a.getX(), b.getX(), a.isSameType(b));

    }
}