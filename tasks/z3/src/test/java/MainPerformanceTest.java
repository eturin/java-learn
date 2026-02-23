

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ture.Main;

class MainPerformanceTest {

    private String shortText;
    private String mediumText;
    private String longText;
    private String veryLongText;
    private String textWithAccents;

    @BeforeEach
    void setUp() {
        shortText = "звони́т догово́р";

        mediumText = "звони́т догово́р щаве́ль катало́г столя́р све́кла балова́ть обеспе́чение краси́вее сре́дства";

        // Генерируем длинный текст повторением
        longText = IntStream.range(0, 100)
                .mapToObj(i -> mediumText)
                .collect(Collectors.joining(" "));

        veryLongText = IntStream.range(0, 1000)
                .mapToObj(i -> "слово" + i)
                .collect(Collectors.joining(" "));

        textWithAccents = "звони́т догово́р щаве́ль катало́г столя́р све́кла балова́ть обеспе́чение краси́вее сре́дства";
    }

    @Test
    @DisplayName("Сравнение производительности TYPE.WORDS на коротком тексте")
    void compareWordsPerformanceShort() {
        // Прогрев JVM
        Main.reverse(shortText, Main.TYPE.WORDS);
        Main.reverse2(shortText, Main.TYPE.WORDS);
        Main.reverse3(shortText, Main.TYPE.WORDS);

        // Тест reverse (ручной)
        long start1 = System.nanoTime();
        String result1 = Main.reverse(shortText, Main.TYPE.WORDS);
        long duration1 = System.nanoTime() - start1;

        // Тест reverse2 (stream)
        long start2 = System.nanoTime();
        String result2 = Main.reverse2(shortText, Main.TYPE.WORDS);
        long duration2 = System.nanoTime() - start2;

        // Тест reverse3 (regex)
        long start3 = System.nanoTime();
        String result3 = Main.reverse3(shortText, Main.TYPE.WORDS);
        long duration3 = System.nanoTime() - start3;

        System.out.println("\n=== TYPE.WORDS на коротком тексте ===");
        System.out.printf("reverse:  %8d нс\n", duration1);
        System.out.printf("reverse2: %8d нс\n", duration2);
        System.out.printf("reverse3: %8d нс\n", duration3);

        assertEquals(result1, result2);
        assertEquals(result1, result3);
    }

    @Test
    @DisplayName("Сравнение производительности TYPE.CHARS на коротком тексте")
    void compareCharsPerformanceShort() {
        // Прогрев
        Main.reverse(shortText, Main.TYPE.CHARS);
        Main.reverse2(shortText, Main.TYPE.CHARS);
        Main.reverse3(shortText, Main.TYPE.CHARS);

        long start1 = System.nanoTime();
        String result1 = Main.reverse(shortText, Main.TYPE.CHARS);
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        String result2 = Main.reverse2(shortText, Main.TYPE.CHARS);
        long duration2 = System.nanoTime() - start2;

        long start3 = System.nanoTime();
        String result3 = Main.reverse3(shortText, Main.TYPE.CHARS);
        long duration3 = System.nanoTime() - start3;

        System.out.println("\n=== TYPE.CHARS на коротком тексте ===");
        System.out.printf("reverse:  %8d нс\n", duration1);
        System.out.printf("reverse2: %8d нс\n", duration2);
        System.out.printf("reverse3: %8d нс\n", duration3);

        // reverse и reverse2 должны дать одинаковый результат
        assertEquals(result1, result2);
        // reverse3 обрабатывает ударения иначе, поэтому результат может отличаться
    }

    @RepeatedTest(5)
    @DisplayName("Повторный тест производительности на среднем тексте")
    void repeatedPerformanceTestMedium() {
        String result1 = Main.reverse(mediumText, Main.TYPE.CHARS);
        String result2 = Main.reverse2(mediumText, Main.TYPE.CHARS);
        String result3 = Main.reverse3(mediumText, Main.TYPE.CHARS);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
    }

    @Test
    @DisplayName("Сравнение производительности на длинном тексте")
    void comparePerformanceLong() {
        final int iterations = 10;
        long total1 = 0, total2 = 0, total3 = 0;

        for (int i = 0; i < iterations; i++) {
            long start1 = System.nanoTime();
            Main.reverse(longText, Main.TYPE.CHARS);
            total1 += System.nanoTime() - start1;

            long start2 = System.nanoTime();
            Main.reverse2(longText, Main.TYPE.CHARS);
            total2 += System.nanoTime() - start2;

            long start3 = System.nanoTime();
            Main.reverse3(longText, Main.TYPE.CHARS);
            total3 += System.nanoTime() - start3;
        }

        long avg1 = total1 / iterations;
        long avg2 = total2 / iterations;
        long avg3 = total3 / iterations;

        System.out.println("\n=== TYPE.CHARS на длинном тексте (среднее за " + iterations + " запусков) ===");
        System.out.printf("reverse:  %8d нс\n", avg1);
        System.out.printf("reverse2: %8d нс\n", avg2);
        System.out.printf("reverse3: %8d нс\n", avg3);

        // reverse2 (stream) обычно медленнее reverse (ручной обход)
        // reverse3 (regex) может быть самым медленным
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "звони́т",
            "звони́т догово́р щаве́ль",
            "звони́т догово́р щаве́ль катало́г столя́р све́кла"
    })
    @DisplayName("Параметризованный тест производительности для разных длин")
    void parameterizedPerformanceTest(String input) {
        System.out.println("\nТест для строки длиной " + input.length() + " символов");

        long start1 = System.nanoTime();
        Main.reverse(input, Main.TYPE.CHARS);
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        Main.reverse2(input, Main.TYPE.CHARS);
        long duration2 = System.nanoTime() - start2;

        long start3 = System.nanoTime();
        Main.reverse3(input, Main.TYPE.CHARS);
        long duration3 = System.nanoTime() - start3;

        System.out.printf("reverse:  %8d нс\n", duration1);
        System.out.printf("reverse2: %8d нс\n", duration2);
        System.out.printf("reverse3: %8d нс\n", duration3);
    }

    @Test
    @DisplayName("Тест масштабирования (линейность)")
    void testScaling() {
        List<Integer> lengths = Arrays.asList(10, 100, 1000, 10000);
        Map<Integer, Long> times1 = new LinkedHashMap<>();
        Map<Integer, Long> times2 = new LinkedHashMap<>();
        Map<Integer, Long> times3 = new LinkedHashMap<>();

        for (int length : lengths) {
            String testString = IntStream.range(0, length)
                    .mapToObj(i -> "a")
                    .collect(Collectors.joining());

            long start1 = System.nanoTime();
            Main.reverse(testString, Main.TYPE.CHARS);
            times1.put(length, System.nanoTime() - start1);

            long start2 = System.nanoTime();
            Main.reverse2(testString, Main.TYPE.CHARS);
            times2.put(length, System.nanoTime() - start2);

            long start3 = System.nanoTime();
            Main.reverse3(testString, Main.TYPE.CHARS);
            times3.put(length, System.nanoTime() - start3);
        }

        System.out.println("\n=== Масштабирование ===");
        System.out.println("Длина\t\treverse\treverse2\treverse3");
        for (int length : lengths) {
            System.out.printf("%d\t\t%d\t%d\t%d\n",
                    length, times1.get(length), times2.get(length), times3.get(length));
        }

        // Проверяем, что время выполнения не растёт экспоненциально
        assertTrue(times1.get(10000) < times1.get(1000) * 15);
        assertTrue(times2.get(10000) < times2.get(1000) * 15);
    }

    @Test
    @DisplayName("Сравнение с StringBuilder.reverse()")
    void compareWithStringBuilder() {
        String simpleText = "hello world";

        // StringBuilder.reverse() не обрабатывает Unicode корректно,
        // но работает очень быстро
        long start1 = System.nanoTime();
        String sbResult = new StringBuilder(simpleText).reverse().toString();
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        String result2 = Main.reverse2(simpleText, Main.TYPE.CHARS);
        long duration2 = System.nanoTime() - start2;

        System.out.println("\n=== Сравнение со StringBuilder ===");
        System.out.printf("StringBuilder: %8d нс\n", duration1);
        System.out.printf("reverse2:      %8d нс\n", duration2);

        assertEquals(sbResult, result2);
    }

    @Test
    @DisplayName("Тест потребления памяти")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // Очищаем GC
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        String result = Main.reverse3(veryLongText, Main.TYPE.CHARS);

        System.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        System.out.println("\n=== Потребление памяти для reverse3 ===");
        System.out.printf("Использовано памяти: %d байт\n", memoryUsed);
        System.out.printf("Длина результата: %d символов\n", result.length());

        assertTrue(memoryUsed < 10_000_000, "Потребление памяти должно быть менее 10 МБ");
    }
}