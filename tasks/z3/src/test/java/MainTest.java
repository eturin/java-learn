

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import ture.Main;

class MainTest {

    @Test
    @DisplayName("Тест реверсирования слов (TYPE.WORDS)")
    void testReverseWords() {
        String input = "звони́т догово́р щаве́ль";
        String expected = "щаве́ль догово́р звони́т";

        assertEquals(expected, Main.reverse3(input, Main.TYPE.WORDS));
    }

    @Test
    @DisplayName("Тест реверсирования символов с учётом ударений (TYPE.CHARS)")
    void testReverseCharsWithAccents() {
        String input = "звони́т";
        // При реверсировании графем: "з" + "в" + "о" + "н" + "и́" + "т" -> "т" + "и́" + "н" + "о" + "в" + "з"
        String expected = "ти́новз";

        assertEquals(expected, Main.reverse3(input, Main.TYPE.CHARS));
    }

    @Test
    @DisplayName("Тест пустой строки")
    void testEmptyString() {
        String input = "";

        assertEquals("", Main.reverse3(input, Main.TYPE.WORDS));
        assertEquals("", Main.reverse3(input, Main.TYPE.CHARS));
    }

    @Test
    @DisplayName("Тест строки с одним словом")
    void testSingleWord() {
        String input = "програ́мма";

        assertEquals("програ́мма", Main.reverse3(input, Main.TYPE.WORDS));
        assertEquals("амма́ргорп", Main.reverse3(input, Main.TYPE.CHARS));
    }

    @Test
    @DisplayName("Тест строки с несколькими пробелами")
    void testMultipleSpaces() {
        String input = "краси́вее   обеспе́чение";

        assertEquals("обеспе́чение   краси́вее", Main.reverse3(input, Main.TYPE.WORDS));
    }

    @Test
    @DisplayName("Тест английских слов без ударений")
    void testEnglishWords() {
        String input = "hello world";

        assertEquals("world hello", Main.reverse3(input, Main.TYPE.WORDS));
        assertEquals("dlrow olleh", Main.reverse3(input, Main.TYPE.CHARS));
    }

    @Test
    @DisplayName("Тест смешанного текста с цифрами и знаками")
    void testMixedText() {
        String input = "тест123 !@#";

        assertEquals("!@# тест123", Main.reverse3(input, Main.TYPE.WORDS));
        assertEquals("#@! 321тсет", Main.reverse3(input, Main.TYPE.CHARS));
    }

    @Test
    @DisplayName("Тест с эмодзи (суррогатные пары)")
    void testEmoji() {
        String input = "привет 👋 мир";

        assertEquals("мир 👋 привет", Main.reverse3(input, Main.TYPE.WORDS));
        // 👋 состоит из двух суррогатов, но reverse3 обрабатывает как одну графему
        assertEquals("рим 👋 тевирп", Main.reverse3(input, Main.TYPE.CHARS));
    }

    @ParameterizedTest
    @MethodSource("provideWordsWithAccents")
    @DisplayName("Параметризованный тест слов с ударениями")
    void testWordsWithAccents(String input, String expectedChars) {
        assertEquals(expectedChars, Main.reverse3(input, Main.TYPE.CHARS));
    }

    private static Stream<Arguments> provideWordsWithAccents() {
        return Stream.of(
                Arguments.of("звони́т", "ти́новз"),
                Arguments.of("догово́р", "ро́вогод"),
                Arguments.of("щаве́ль", "ьле́ващ"),
                Arguments.of("катало́г", "го́латак"),
                Arguments.of("столя́р", "ря́лотс"),
                Arguments.of("све́кла", "алке́вс"),
                Arguments.of("балова́ть", "ьта́волаб"),
                Arguments.of("обеспе́чение", "еинече́псебо"),
                Arguments.of("краси́вее", "ееви́сарк"),
                Arguments.of("сре́дства", "автсде́рс")
        );
    }

    @Test
    @DisplayName("Сравнение всех трёх методов reverse")
    void testAllReverseMethods() {
        String input = "звони́т догово́р";

        // reverse (ручной обход кодовых точек)
        String result1 = Main.reverse(input, Main.TYPE.CHARS);

        // reverse2 (с использованием Stream API)
        String result2 = Main.reverse2(input, Main.TYPE.CHARS);

        // reverse3 (с использованием regex \\X)
        String result3 = Main.reverse3(input, Main.TYPE.CHARS);

        // Все методы должны дать одинаковый результат для обычных символов
        // Но для ударений reverse3 работает корректно
        assertEquals(result1, result2);
        assertNotEquals(result1, result3); // reverse3 обрабатывает ударения иначе
    }

    @Test
    @DisplayName("Тест производительности")
    void testPerformance() {
        String input = "звони́т догово́р щаве́ль катало́г столя́р све́кла балова́ть обеспе́чение краси́вее сре́дства";

        long startTime = System.nanoTime();
        Main.reverse3(input, Main.TYPE.CHARS);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000; // миллисекунды
        assertTrue(duration < 100, "Метод должен выполняться быстрее 100 мс");
    }
}