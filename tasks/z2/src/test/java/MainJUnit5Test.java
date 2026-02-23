import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import ture.Main;

class MainJUnit5Test {

    @Test
    void testFirstUniqCharBasic() {
        assertEquals("w", Main.getFirstUniqChar("swiss"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aabbcc", "aaaa", "abab", ""})
    void testFirstUniqCharNoUnique(String input) {
        assertNull(Main.getFirstUniqChar(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testFirstUniqCharNullOrEmpty(String input) {
        assertNull(Main.getFirstUniqChar(input));
    }

    @ParameterizedTest
    @MethodSource("provideStringsForTest")
    void testFirstUniqCharParameterized(String input, String expected) {
        assertEquals(expected, Main.getFirstUniqChar(input));
    }

    private static Stream<Arguments> provideStringsForTest() {
        return Stream.of(
                Arguments.of("swiss", "w"),
                Arguments.of("hello", "h"),
                Arguments.of("aabbccddec", "e"),
                Arguments.of("abcde", "a"),
                Arguments.of("a", "a"),
                Arguments.of("😀😀😁😂😁", "😂"),
                Arguments.of("racecar", "e"),
                Arguments.of("programming", "p")
        );
    }

    @Test
    void testFirstUniqCharPerformance() {
        // Тест производительности для длинной строки
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        sb.append("b");

        long startTime = System.currentTimeMillis();
        String result = Main.getFirstUniqChar(sb.toString());
        long endTime = System.currentTimeMillis();

        assertEquals("b", result);
        assertTrue((endTime - startTime) < 100); // должно выполниться меньше чем за 100мс
    }
}