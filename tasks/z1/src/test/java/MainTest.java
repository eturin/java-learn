

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.stream.Stream;
import ture.Main;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeAll
    static void setUpBeforeAll() {
        System.out.println("Начинаем тестирование методов Main");
    }

    @BeforeEach
    void setUpBeforeEach() {
        System.out.println("\n--- Запуск нового теста ---");
    }

    @AfterEach
    void tearDownAfterEach() {
        System.out.println("--- Тест завершён ---");
    }

    @AfterAll
    static void tearDownAfterAll() {
        System.out.println("\nВсе тесты завершены!");
    }

    // ТЕСТЫ ДЛЯ МЕТОДА getRepeated3 (правильный подсчёт)
    @Nested
    @DisplayName("Тесты для getRepeated3")
    class CorrectTests {

        @Test
        @DisplayName("Тест с простой строкой")
        void testSimpleString() {
            String input = "hello world";
            Map<Character, Long> expected = Map.of('l', 2L, 'o', 1L);

            Map<Character, Long> result = Main.getRepeated3(input);

            assertEquals(expected, result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey('l'));
            assertTrue(result.containsKey('o'));
            assertEquals(2L, result.get('l'));
        }

        @Test
        @DisplayName("Тест со строкой без повторений")
        void testNoRepeats() {
            String input = "abcdef";
            Map<Character, Long> result = Main.getRepeated3(input);

            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Тест с одним символом много раз")
        void testSingleCharRepeated() {
            String input = "aaaaaa";
            Map<Character, Long> expected = Map.of('a', 5L);

            Map<Character, Long> result = Main.getRepeated3(input);

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(expected, result);
            assertEquals(5L, result.get('a'));
        }

        @Test
        @DisplayName("Тест с пустой строкой")
        void testEmptyString() {
            String input = "";
            Map<Character, Long> result = Main.getRepeated3(input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Тест с русскими символами")
        void testRussianCharacters() {
            String input = "привет мир";
            Map<Character, Long> expected = Map.of('р', 1L, 'и', 1L);

            Map<Character, Long> result = Main.getRepeated3(input);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Тест с цифрами и спецсимволами")
        void testDigitsAndSpecialChars() {
            String input = "a1b2c3a1b2c";
            Map<Character, Long> expected = Map.of(
                    'a', 1L, '1', 1L, 'b', 1L, '2', 1L, 'c', 1L
            );

            Map<Character, Long> result = Main.getRepeated3(input);

            assertEquals(expected, result);
            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("Тест с пробелами")
        void testSpaces() {
            String input = "  a  b  a  ";
            Map<Character, Long> expected = Map.of(' ', 7L, 'a', 1L);

            Map<Character, Long> result = Main.getRepeated3(input);

            assertEquals(expected, result);
            assertEquals(7L, result.get(' '));
        }

        @Test
        @DisplayName("Тест на регистрозависимость")
        void testCaseSensitivity() {
            String input = "AaBbCcAaa";
            Map<Character, Long> expected = Map.of('A', 1L, 'a', 2L);

            Map<Character, Long> result = Main.getRepeated3(input);

            assertEquals(expected, result);
            assertEquals(2, result.size());
            assertNotEquals(result.get('A'), result.get('a')); // Разные символы
        }
    }

    // ТЕСТЫ ДЛЯ МЕТОДА getRepeated3
    @Nested
    @DisplayName("Тесты для getRepeated")
    class getRepeatedTests {

        @Test
        @DisplayName("Тест с простой строкой")
        void testSimpleString() {
            String input = "hello world";
            Map<Character, Long> expected = Map.of('l', 2L, 'o', 1L);

            Map<Character, Long> result = Main.getRepeated(input);

            assertEquals(expected, result);
            assertEquals(2, result.size());
            assertEquals(2L, result.get('l'));
            assertEquals(1L, result.get('o'));
        }

        @Test
        @DisplayName("Тест с одним символом много раз")
        void testSingleCharRepeated() {
            String input = "aaaaaa";
            Map<Character, Long> expected = Map.of('a', 5L);

            Map<Character, Long> result = Main.getRepeated(input);

            assertEquals(expected, result);
            assertEquals(5L, result.get('a'));
        }

        @Test
        @DisplayName("Тест с символом, встречающимся 1 раз")
        void testSingleOccurrence() {
            String input = "abc";
            Map<Character, Long> result = Main.getRepeated(input);

            assertTrue(result.isEmpty());
        }
    }

    // ПАРАМЕТРИЗОВАННЫЕ ТЕСТЫ
    @Nested
    @DisplayName("Параметризованные тесты")
    class ParameterizedTests {

        @ParameterizedTest
        @MethodSource("stringProvider")
        @DisplayName("Тест с разными строками")
        void testWithMethodSource(String input, Map<Character, Long> expected) {
            Map<Character, Long> result = Main.getRepeated(input);
            assertEquals(expected, result);
        }

        static Stream<Arguments> stringProvider() {
            return Stream.of(
                    Arguments.of("hello", Map.of('l', 1L)),
                    Arguments.of("test", Map.of('t', 1L)),
                    Arguments.of("aabbcc", Map.of('a', 1L, 'b', 1L, 'c', 1L)),
                    Arguments.of("", Map.of()),
                    Arguments.of("abcde", Map.of())
            );
        }

        @ParameterizedTest
        @CsvSource({
                "hello, l:1",
                "test, t:1",
                "aabbcc, a:1;b:1;c:1"
        })
        @DisplayName("Тест с CSV источником")
        void testWithCsvSource(String input, String expectedStr) {
            Map<Character, Long> result = Main.getRepeated(input);

            if (expectedStr.equals("''")) {
                assertTrue(result.isEmpty());
            } else {
                String[] pairs = expectedStr.split(";");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    Character key = kv[0].charAt(0);
                    Long value = Long.parseLong(kv[1]);
                    assertEquals(value, result.get(key));
                }
            }
        }
    }

    // ТЕСТЫ ДЛЯ СРАВНЕНИЯ РАЗНЫХ МЕТОДОВ
    @Nested
    @DisplayName("Сравнение всех методов")
    class ComparisonTests {

        @Test
        @DisplayName("Сравнение getRepeatdd и countRepetitions")
        void compareMethods() {
            String input = "hello world";

            Map<Character, Long> result1 = Main.getRepeated(input);
            Map<Character, Long> result2 = Main.getRepeated3(input);


            // getRepeatdd и getRepeatdd2 возвращают одинаковый результат
            assertEquals(result1, result2);


            // Проверяем, что result1 содержит "лишние" символы с 0
            assertFalse(result1.containsKey(' '));
            assertFalse(result1.containsKey('h'));
        }
    }

    // ТЕСТЫ НА ИСКЛЮЧЕНИЯ
    @Nested
    @DisplayName("Тесты на исключения")
    class ExceptionTests {

        @Test
        @DisplayName("Тест с null строкой")
        void testNullString() {
            assertThrows(NullPointerException.class, () -> {
                Main.getRepeated(null);
            });
        }
    }

    // ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ (опционально)
    @Nested
    @DisplayName("Тесты производительности")
    class PerformanceTests {

        @Test
        @DisplayName("Сравнение скорости методов")
        void testPerformance() {
            String input = "a".repeat(10000) + "b".repeat(5000) + "c".repeat(1000);

            long startTime = System.nanoTime();
            Map<Character, Long> result3 = Main.getRepeated3(input);
            long endTime = System.nanoTime();
            long duration3 = endTime - startTime;

            startTime = System.nanoTime();
            Map<Character, Long> result2 = Main.getRepeated2(input);
            endTime = System.nanoTime();
            long duration2 = endTime - startTime;


            startTime = System.nanoTime();
            Map<Character, Long> result1 = Main.getRepeated(input);
            endTime = System.nanoTime();
            long duration1 = endTime - startTime;

            System.out.println("Время getRepeated3: " + duration3 / 1_000_000.0 + " мс");
            System.out.println("Время getRepeated2: " + duration2 / 1_000_000.0 + " мс");
            System.out.println("Время getRepeated: " + duration1 / 1_000_000.0 + " мс");

            // Проверяем корректность
            assertEquals(9999L, result1.get('a'));
            assertEquals(4999L, result1.get('b'));
            assertEquals(999L, result1.get('c'));

            assertEquals(9999L, result2.get('a'));
            assertEquals(4999L, result2.get('b'));
            assertEquals(999L, result2.get('c'));
        }
    }
}