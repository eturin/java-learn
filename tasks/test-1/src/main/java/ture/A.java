package ture;

public class A<T> {
    T x;
    A(T x) {
        this.x = x;
    }

    T getX() {
        return this.x;
    }
    boolean isSameType(A<?> other) {
        return this.x.getClass().getName().equals(other.x.getClass().getName());
    }
}
