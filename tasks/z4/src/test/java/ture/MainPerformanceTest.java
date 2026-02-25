package ture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;


import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты производительности для проверки строк на цифры")
public class MainPerformanceTest {

    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASURED_ITERATIONS = 10;

    private String shortNumericString;
    private String longNumericString;
    private String shortMixedString;
    private String longMixedString;
    private String emptyString;
    private String veryLongNumericString;

    @BeforeEach
    void setUp() {
        shortNumericString = "1234567890";
        longNumericString = generateNumericString(10000);
        shortMixedString = "123abc456";
        longMixedString = generateMixedString(10000);
        emptyString = "";
        veryLongNumericString = generateNumericString(100000);
    }

    private String generateNumericString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateMixedString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Test
    @DisplayName("Проверка корректности обоих методов")
    void testCorrectness() {
        // Проверяем, что оба метода дают одинаковые результаты
        assertEquals(Main.check(shortNumericString), Main.check2(shortNumericString));
        assertEquals(Main.check(shortMixedString), Main.check2(shortMixedString));
        assertEquals(Main.check(longNumericString), Main.check2(longNumericString));
        assertEquals(Main.check(longMixedString), Main.check2(longMixedString));
        assertEquals(Main.check(emptyString), Main.check2(emptyString));
    }

    @RepeatedTest(5)
    @DisplayName("Тест производительности: короткая числовая строка")
    void testShortNumericPerformance() {
        long startTime = System.nanoTime();
        boolean result1 = Main.check(shortNumericString);
        long time1 = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean result2 = Main.check2(shortNumericString);
        long time2 = System.nanoTime() - startTime;

        System.out.printf("Короткая числовая строка (%d символов):\n", shortNumericString.length());
        System.out.printf("  check()  (stream): %d нс\n", time1);
        System.out.printf("  check2() (loop)  : %d нс\n", time2);
        System.out.printf("  Разница: %.2fx\n", (double)Math.max(time1, time2) / Math.min(time1, time2));

        assertEquals(result1, result2);
    }

    @RepeatedTest(5)
    @DisplayName("Тест производительности: длинная числовая строка")
    void testLongNumericPerformance() {
        long startTime = System.nanoTime();
        boolean result1 = Main.check(longNumericString);
        long time1 = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean result2 = Main.check2(longNumericString);
        long time2 = System.nanoTime() - startTime;

        System.out.printf("\nДлинная числовая строка (%d символов):\n", longNumericString.length());
        System.out.printf("  check()  (stream): %d нс (%.2f мс)\n", time1, time1 / 1_000_000.0);
        System.out.printf("  check2() (loop)  : %d нс (%.2f мс)\n", time2, time2 / 1_000_000.0);
        System.out.printf("  Разница: %.2fx\n", (double)Math.max(time1, time2) / Math.min(time1, time2));

        assertEquals(result1, result2);
    }

    @RepeatedTest(5)
    @DisplayName("Тест производительности: строка со смешанными символами")
    void testMixedPerformance() {
        long startTime = System.nanoTime();
        boolean result1 = Main.check(shortMixedString);
        long time1 = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean result2 = Main.check2(shortMixedString);
        long time2 = System.nanoTime() - startTime;

        System.out.printf("\nКороткая смешанная строка (%d символов):\n", shortMixedString.length());
        System.out.printf("  check()  (stream): %d нс\n", time1);
        System.out.printf("  check2() (loop)  : %d нс\n", time2);
        System.out.printf("  Разница: %.2fx\n", (double)Math.max(time1, time2) / Math.min(time1, time2));

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    @DisplayName("Сравнительный тест производительности с разными длинами строк")
    void comparativePerformanceTest() {
        System.out.println("\n=== СРАВНИТЕЛЬНЫЙ ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ===");

        int[] lengths = {10, 100, 1000, 10000, 100000};

        for (int length : lengths) {
            String numericStr = generateNumericString(length);

            // Прогрев JVM
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                Main.check(numericStr);
                Main.check2(numericStr);
            }

            // Измерение
            long totalTime1 = 0;
            long totalTime2 = 0;

            for (int i = 0; i < MEASURED_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                Main.check(numericStr);
                totalTime1 += System.nanoTime() - startTime;

                startTime = System.nanoTime();
                Main.check2(numericStr);
                totalTime2 += System.nanoTime() - startTime;
            }

            long avgTime1 = totalTime1 / MEASURED_ITERATIONS;
            long avgTime2 = totalTime2 / MEASURED_ITERATIONS;

            System.out.printf("\nДлина строки: %d\n", length);
            System.out.printf("  check()  (stream) среднее: %d нс (%.2f мс)\n", avgTime1, avgTime1 / 1_000_000.0);
            System.out.printf("  check2() (loop)   среднее: %d нс (%.2f мс)\n", avgTime2, avgTime2 / 1_000_000.0);
            System.out.printf("  Соотношение (loop/stream): %.2f\n", (double)avgTime2 / avgTime1);
        }
    }

    @Test
    @DisplayName("Тест на пустой строке")
    void testEmptyString() {
        long startTime = System.nanoTime();
        boolean result1 = Main.check(emptyString);
        long time1 = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean result2 = Main.check2(emptyString);
        long time2 = System.nanoTime() - startTime;

        System.out.printf("\nПустая строка:\n");
        System.out.printf("  check()  (stream): %d нс\n", time1);
        System.out.printf("  check2() (loop)  : %d нс\n", time2);

        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    @DisplayName("Тест с очень длинной строкой")
    void testVeryLongString() {
        System.out.printf("\nОчень длинная строка (%d символов):\n", veryLongNumericString.length());

        // Прогрев
        for (int i = 0; i < 3; i++) {
            Main.check(veryLongNumericString);
            Main.check2(veryLongNumericString);
        }

        long startTime = System.nanoTime();
        boolean result1 = Main.check(veryLongNumericString);
        long time1 = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        boolean result2 = Main.check2(veryLongNumericString);
        long time2 = System.nanoTime() - startTime;

        System.out.printf("  check()  (stream): %d нс (%.2f мс)\n", time1, time1 / 1_000_000.0);
        System.out.printf("  check2() (loop)  : %d нс (%.2f мс)\n", time2, time2 / 1_000_000.0);
        System.out.printf("  Разница: %.2fx\n", (double)Math.max(time1, time2) / Math.min(time1, time2));

        assertEquals(result1, result2);
    }
}