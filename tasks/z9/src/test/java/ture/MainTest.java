package ture;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testJoin() {
        // Простые тесты для вашего метода
        assertEquals("a,b,c", Main.join(",", new String[]{"a", "b", "c"}));
        assertEquals("hello", Main.join("-", new String[]{"hello"}));
        assertEquals("", Main.join(",", new String[]{}));
        assertEquals("a b c", Main.join(" ", new String[]{"a", "b", "c"}));
        assertEquals("a!!b!!c", Main.join("!!", new String[]{"a", "b", "c"}));
    }

    @Test
    void testWithDifferentDelimiters() {
        String[] words = {"Hello", "World"};

        assertEquals("Hello,World", Main.join(",", words));
        assertEquals("Hello World", Main.join(" ", words));
        assertEquals("Hello-World", Main.join("-", words));
        assertEquals("HelloWorld", Main.join("", words));
    }

    @Test
    void testEdgeCases() {
        assertEquals("", Main.join(",", new String[]{}));
        assertEquals("one", Main.join(",", new String[]{"one"}));

        String[] withNull = {"a", null, "c"};
        assertEquals("a,null,c", Main.join(",", withNull));
    }
}