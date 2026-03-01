package ture;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainNumberConversionTest {

    @Test
    @Order(0)
    @DisplayName("Базовые тесты для целых чисел")
    void testBasicIntegers() {
        System.out.println("Тестирование базовых целых чисел");

        // Целые числа в диапазоне int
        Main.Numbers result = Main.toNumbers("123");
        assertEquals(123, result.n());
        assertEquals(123L, result.l());
        assertEquals(123.0f, result.f(), 0.0001);
        assertEquals(123.0, result.d(), 0.0001);

        // Отрицательные числа
        result = Main.toNumbers("-456");
        assertEquals(-456, result.n());
        assertEquals(-456L, result.l());
        assertEquals(-456.0f, result.f(), 0.0001);
        assertEquals(-456.0, result.d(), 0.0001);

        // Ноль
        result = Main.toNumbers("0");
        assertEquals(0, result.n());
        assertEquals(0L, result.l());
        assertEquals(0.0f, result.f(), 0.0001);
        assertEquals(0.0, result.d(), 0.0001);
    }

    @Test
    @Order(1)
    @DisplayName("Тесты для чисел с плавающей точкой")
    void testFloatingPoint() {
        System.out.println("Тестирование чисел с плавающей точкой");

        // Простые десятичные дроби
        Main.Numbers result = Main.toNumbers("123.456");
        assertNull(result.n()); // Не может быть int
        assertNull(result.l()); // Не может быть long
        assertEquals(123.456f, result.f(), 0.0001);
        assertEquals(123.456, result.d(), 0.0001);

        // Научная нотация
        result = Main.toNumbers("1.23e-4");
        assertNull(result.n());
        assertNull(result.l());
        assertEquals(0.000123f, result.f(), 0.0000001);
        assertEquals(0.000123, result.d(), 0.0000001);

        // Большая экспонента
        result = Main.toNumbers("1.23E10");
        assertEquals(1.23E10f, result.f(), 1.0);
        assertEquals(1.23E10, result.d(), 1.0);
    }

    @Test
    @Order(2)
    @DisplayName("Граничные значения для int")
    void testIntBoundaries() {
        System.out.println("Тестирование граничных значений int");

        // Максимальное значение int
        Main.Numbers result = Main.toNumbers(String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, result.n());
        assertEquals((long) Integer.MAX_VALUE, result.l());

        // Минимальное значение int
        result = Main.toNumbers(String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, result.n());
        assertEquals((long) Integer.MIN_VALUE, result.l());

        // Чуть больше максимального int
        String beyondInt = String.valueOf(Integer.MAX_VALUE + 1L);
        result = Main.toNumbers(beyondInt);
        assertNull(result.n()); // Не может быть int
        assertEquals(Integer.MAX_VALUE + 1L, result.l());
    }

    @Test
    @Order(3)
    @DisplayName("Граничные значения для long")
    void testLongBoundaries() {
        System.out.println("Тестирование граничных значений long");

        // Максимальное значение long
        Main.Numbers result = Main.toNumbers(String.valueOf(Long.MAX_VALUE));
        assertNull(result.n()); // Слишком большое для int
        assertEquals(Long.MAX_VALUE, result.l());

        // Минимальное значение long
        result = Main.toNumbers(String.valueOf(Long.MIN_VALUE));
        assertNull(result.n());
        assertEquals(Long.MIN_VALUE, result.l());

        // Число в long, но не в int
        long longValue = Integer.MAX_VALUE + 1L;
        result = Main.toNumbers(String.valueOf(longValue));
        assertNull(result.n());
        assertEquals(longValue, result.l());
    }

    @Test
    @Order(4)
    @DisplayName("Граничные значения для float и double")
    void testFloatDoubleBoundaries() {
        System.out.println("Тестирование граничных значений float/double");

        // Максимальное значение float
        String maxFloat = String.valueOf(Float.MAX_VALUE);
        Main.Numbers result = Main.toNumbers(maxFloat);
        assertNull(result.n());
        assertNull(result.l());
        assertEquals(Float.MAX_VALUE, result.f(), Float.MAX_VALUE / 1e6);
        assertEquals((double) Float.MAX_VALUE, result.d(), Float.MAX_VALUE / 1e6);

        // Минимальное положительное float
        String minFloat = String.valueOf(Float.MIN_VALUE);
        result = Main.toNumbers(minFloat);
        assertEquals(Float.MIN_VALUE, result.f(), Float.MIN_VALUE / 1e6);

        // Бесконечность (должна обрабатываться как число с плавающей точкой)
        result = Main.toNumbers("Infinity");
        assertNull(result.n());
        assertNull(result.l());
        assertEquals(Float.POSITIVE_INFINITY, result.f());
        assertEquals(Double.POSITIVE_INFINITY, result.d());
    }

    @ParameterizedTest
    @Order(5)
    @MethodSource("provideValidNumbers")
    @DisplayName("Параметризованный тест для валидных чисел")
    void testValidNumbers(String input,
                          Integer expectedInt,
                          Long expectedLong,
                          Float expectedFloat,
                          Double expectedDouble) {

        System.out.println("Тестирование: " + input);
        Main.Numbers result = Main.toNumbers(input);

        assertEquals(expectedInt, result.n());
        assertEquals(expectedLong, result.l());

        if (expectedFloat != null) {
            assertEquals(expectedFloat, result.f(), 0.0001);
        } else {
            assertNull(result.f());
        }

        if (expectedDouble != null) {
            assertEquals(expectedDouble, result.d(), 0.0001);
        } else {
            assertNull(result.d());
        }
    }

    private static Stream<Arguments> provideValidNumbers() {
        return Stream.of(
                // Простые целые
                Arguments.of("0", 0, 0L, 0.0f, 0.0),
                Arguments.of("42", 42, 42L, 42.0f, 42.0),
                Arguments.of("-99", -99, -99L, -99.0f, -99.0),

                // Десятичные дроби
                Arguments.of("3.14", null, null, 3.14f, 3.14),
                Arguments.of("-2.5", null, null, -2.5f, -2.5),

                // Научная нотация
                Arguments.of("1e3", null, null, 1000.0f, 1000.0),
                Arguments.of("1.5e-2", null, null, 0.015f, 0.015),

                // Специальные значения
                Arguments.of("NaN", null, null, Float.NaN, Double.NaN),
                Arguments.of("Infinity", null, null, Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Arguments.of("-Infinity", null, null, Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
        );
    }

    @ParameterizedTest
    @Order(6)
    @ValueSource(strings = {
            "",           // Пустая строка
            "abc",        // Не число
            "123abc",     // Число с буквами
            "12 34",      // Пробел внутри
            "--123",      // Двойной минус
            "++456",      // Двойной плюс
            "12.34.56",   // Две точки
            "1e",         // Неполная экспонента
            "e5",         // Только экспонента
            " ",          // Пробел
            "\t",         // Табуляция
            "\n"          // Новая строка
    })
    @DisplayName("Тесты для невалидных строк")
    void testInvalidStrings(String input) {
        System.out.println("Тестирование невалидной строки: \"" + input + "\"");
        Main.Numbers result = Main.toNumbers(input);

        assertAll("Все поля должны быть null",
                () -> assertNull(result.n()),
                () -> assertNull(result.l()),
                () -> assertNull(result.f()),
                () -> assertNull(result.d())
        );
    }

    @Test
    @Order(7)
    @DisplayName("Тест с ведущими нулями")
    void testLeadingZeros() {
        System.out.println("Тестирование чисел с ведущими нулями");

        Main.Numbers result = Main.toNumbers("007");
        assertEquals(7, result.n());
        assertEquals(7L, result.l());
        assertEquals(7.0f, result.f(), 0.0001);
        assertEquals(7.0, result.d(), 0.0001);

        result = Main.toNumbers("-007");
        assertEquals(-7, result.n());
        assertEquals(-7L, result.l());
    }

    @Test
    @Order(8)
    @DisplayName("Тест с пробелами вокруг числа")
    void testWhitespace() {
        System.out.println("Тестирование пробелов вокруг числа");

        // Пробелы в начале и конце - должны работать!
        Main.Numbers result = Main.toNumbers("  123  ");
        assertEquals(123, result.n());
        assertEquals(123L, result.l());
        assertEquals(123.0f, result.f(), 0.0001);
        assertEquals(123.0, result.d(), 0.0001);

        // Пробелы внутри - не должны работать
        result = Main.toNumbers("12 3");
        Main.Numbers finalResult = result;
        assertAll("Все поля null",
                () -> assertNull(finalResult.n()),
                () -> assertNull(finalResult.l()),
                () -> assertNull(finalResult.f()),
                () -> assertNull(finalResult.d())
        );
    }

    @Test
    @Order(9)
    @DisplayName("Тест со знаком плюс")
    void testPlusSign() {
        System.out.println("Тестирование явного знака плюс");

        Main.Numbers result = Main.toNumbers("+123");
        assertEquals(123, result.n());
        assertEquals(123L, result.l());
        assertEquals(123.0f, result.f(), 0.0001);
        assertEquals(123.0, result.d(), 0.0001);

        result = Main.toNumbers("+123.45");
        assertNull(result.n());
        assertNull(result.l());
        assertEquals(123.45f, result.f(), 0.0001);
        assertEquals(123.45, result.d(), 0.0001);
    }

    @Test
    @Order(10)
    @DisplayName("Тест производительности конвертации")
    void testPerformance() {
        System.out.println("\n=== Тест производительности ===");

        String[] testStrings = {
                "123",
                "123456789",
                "123.456",
                "1.23e-10",
                "abc",  // невалидное
                "",     // пустое
                Long.toString(Long.MAX_VALUE)
        };

        int iterations = 100000;

        for (String test : testStrings) {
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                Main.toNumbers(test);
            }
            long time = System.nanoTime() - start;

            System.out.printf("'%s' -> %.2f ns/операцию%n",
                    test, (double) time / iterations);
        }
    }

    @Test
    @Order(11)
    @DisplayName("Тест на точность float vs double")
    void testPrecision() {
        System.out.println("Тестирование точности float vs double");

        String preciseNumber = "123.4567890123456789";
        Main.Numbers result = Main.toNumbers(preciseNumber);

        assertNull(result.n());
        assertNull(result.l());

        float f = result.f();
        double d = result.d();

        System.out.printf("Original: %s%n", preciseNumber);
        System.out.printf("Float:    %.15f%n", f);
        System.out.printf("Double:   %.15f%n", d);

        // Сравниваем с оригинальным строковым представлением
        double expected = 123.45678901234567; // Более точное представление
        double floatAsDouble = f; // Автоматическое расширение float до double

        System.out.printf("Разница float-original: %.15f%n", Math.abs(floatAsDouble - expected));
        System.out.printf("Разница double-original: %.15f%n", Math.abs(d - expected));

        // Float должен иметь бОльшую погрешность
        assertTrue(Math.abs(floatAsDouble - expected) > Math.abs(d - expected),
                "Float должен иметь бОльшую погрешность чем double");

        // Проверяем, что float действительно теряет точность
        // Float имеет ~7 значащих цифр, double ~15
        String floatStr = String.format("%.10f", f);
        String doubleStr = String.format("%.10f", d);

        assertNotEquals(floatStr, doubleStr, "Float и double должны давать разные результаты");
    }

    // Альтернативный вариант с параметризацией
    @ParameterizedTest
    @Order(12)
    @CsvSource({
            "0.1, 0.1",
            "0.2, 0.2",
            "0.3, 0.30000000000000004", // Классический пример неточности double
            "123.45678901234567, 123.45678901234568"
    })
    @DisplayName("Демонстрация неточности floating point")
    void testFloatingPointImprecision(String input, String expectedDouble) {
        System.out.println("\nТестирование: " + input);

        Main.Numbers result = Main.toNumbers(input);

        float f = result.f();
        double d = result.d();

        System.out.printf("Float:  %.20f%n", f);
        System.out.printf("Double: %.20f%n", d);

        // Демонстрируем, что float и double дают разные результаты
        assertNotEquals(f, d, 0.0);
    }

    @Test
    @Order(12)
    @DisplayName("Тест всех полей одновременно")
    void testAllFields() {
        System.out.println("Тестирование всех полей одновременно");

        // Число, которое подходит под все типы
        Main.Numbers result = Main.toNumbers("42");

        Main.Numbers finalResult = result;
        assertAll("Проверка всех полей",
                () -> assertEquals(42, finalResult.n()),
                () -> assertEquals(42L, finalResult.l()),
                () -> assertEquals(42.0f, finalResult.f(), 0.0001f),
                () -> assertEquals(42.0, finalResult.d(), 0.0001)
        );

        // Число, которое подходит только под float/double
        result = Main.toNumbers("42.5");

        Main.Numbers finalResult1 = result;
        assertAll("Только float/double",
                () -> assertNull(finalResult1.n()),
                () -> assertNull(finalResult1.l()),
                () -> assertEquals(42.5f, finalResult1.f(), 0.0001f),
                () -> assertEquals(42.5, finalResult1.d(), 0.0001)
        );
    }

    @Test
    @Order(13)
    @DisplayName("Тест с очень большими числами")
    void testVeryLargeNumbers() {
        System.out.println("Тестирование очень больших чисел");

        // Число больше long, но меньше double
        String veryLarge = "1" + "0".repeat(20); // 10^20
        Main.Numbers result = Main.toNumbers(veryLarge);

        assertNull(result.n());
        assertNull(result.l());
        assertNotNull(result.f());
        assertNotNull(result.d());

        System.out.println("10^20 как float: " + result.f());
        System.out.println("10^20 как double: " + result.d());
    }

    @AfterEach
    void tearDown() {
        System.out.println("✓ Тест завершен");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("\n=== Все тесты конвертации чисел завершены ===");
    }
}