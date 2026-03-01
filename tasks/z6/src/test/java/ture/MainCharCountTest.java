package ture;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT) // Параллельное выполнение тестов
class MainCharCountTest {

    private static final int WARMUP_ITERATIONS = 20;
    private static final int MEASUREMENT_ITERATIONS = 30;

    private String smallString;
    private String mediumString;
    private String largeString;
    private String hugeString;
    private String repeatedCharString;
    private String noMatchesString;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Подготовка тестовых данных ===");
        smallString = generateRandomString(100, 42);
        mediumString = generateRandomString(1_000, 43);
        largeString = generateRandomString(100_000, 44);
        hugeString = generateRandomString(1_000_000, 45);

        // Специальные строки для тестов
        repeatedCharString = "a".repeat(1_000_000);
        noMatchesString = "b".repeat(1_000_000);

        // Проверяем что оба метода дают одинаковый результат
        char testChar = 'a';
        long result1 = Main.charCount(smallString, testChar);
        long result2 = Main.charCount2(smallString, testChar);

        System.out.printf("Строка 100: поиск '%c' -> %d совпадений%n",
                testChar, result1);

        assertEquals(result1, result2, "Методы должны давать одинаковый результат");
    }

    private String generateRandomString(int length, int seed) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) ('a' + random.nextInt(26));
            if (random.nextBoolean()) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Test
    @Order(0)
    @DisplayName("Базовые тесты функциональности")
    void testBasicFunctionality() {
        System.out.println("Базовые тесты функциональности");

        String testStr = "hello world";

        // Поиск существующего символа
        assertAll("Поиск существующих символов",
                () -> assertEquals(1, Main.charCount(testStr, 'h')),
                () -> assertEquals(1, Main.charCount2(testStr, 'h')),
                () -> assertEquals(3, Main.charCount(testStr, 'l')),
                () -> assertEquals(3, Main.charCount2(testStr, 'l'))
        );

        // Поиск несуществующего символа
        assertAll("Поиск несуществующих символов",
                () -> assertEquals(0, Main.charCount(testStr, 'z')),
                () -> assertEquals(0, Main.charCount2(testStr, 'z'))
        );

        // Поиск с учетом регистра
        assertAll("Проверка регистрозависимости",
                () -> assertEquals(1, Main.charCount("Hello", 'H')),
                () -> assertEquals(1, Main.charCount2("Hello", 'H')),
                () -> assertEquals(0, Main.charCount("Hello", 'h')),
                () -> assertEquals(0, Main.charCount2("Hello", 'h'))
        );

        // Пустая строка
        assertAll("Пустая строка",
                () -> assertEquals(0, Main.charCount("", 'a')),
                () -> assertEquals(0, Main.charCount2("", 'a'))
        );

        System.out.println("✓ Все базовые тесты пройдены!");
    }

    @Order(1)
    @DisplayName("Параметризованный тест")
    @ParameterizedTest
    @CsvSource({
            "hello, h, 1",
            "hello, l, 2",
            "hello, z, 0",
            "aaaaa, a, 5",
            "AbAcA, A, 3"
    })
    void testParameterized(String str, char ch, long expected) {
        assertEquals(expected, Main.charCount(str, ch));
        assertEquals(expected, Main.charCount2(str, ch));
    }

    @Test
    @Order(2)
    @DisplayName("Тест стабильности с прогревом")
    void testStabilityWithWarmup() {
        System.out.println("Тест стабильности с прогревом");
        char searchChar = 'a';

        // Прогревочные итерации
        System.out.print("Прогрев JIT компилятора");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Main.charCount(mediumString, searchChar);
            Main.charCount2(mediumString, searchChar);
            if (i % 5 == 0) System.out.print(".");
        }
        System.out.println(" готово!");

        // Измерения
        List<Long> countTimes = new ArrayList<>();
        List<Long> count2Times = new ArrayList<>();

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            countTimes.add(measureTime(() -> Main.charCount(mediumString, searchChar), 1));
            count2Times.add(measureTime(() -> Main.charCount2(mediumString, searchChar), 1));
        }

        // Статистика
        double avgCount = countTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgCount2 = count2Times.stream().mapToLong(Long::longValue).average().orElse(0);
        double stdDevCount = calculateStdDev(countTimes);
        double stdDevCount2 = calculateStdDev(count2Times);

        System.out.printf(Locale.US, "charCount()  среднее: %.0f ns, стд.откл.: %.0f (%.1f%%)%n",
                avgCount, stdDevCount, (stdDevCount / avgCount * 100));
        System.out.printf(Locale.US, "charCount2() среднее: %.0f ns, стд.откл.: %.0f (%.1f%%)%n",
                avgCount2, stdDevCount2, (stdDevCount2 / avgCount2 * 100));

        // Для stream API допускаем больший разброс
        assertTrue(stdDevCount / avgCount < 0.3, "Слишком большой разброс в charCount()");
        assertTrue(stdDevCount2 / avgCount2 < 0.5, "Слишком большой разброс в charCount2()");
    }

    @Test
    @Order(3)
    @DisplayName("Сравнение производительности на разных размерах")
    void testComparePerformance() {
        int[] sizes = {100, 1_000, 10_000, 100_000, 1_000_000};
        char searchChar = 'a';

        System.out.println("\n=== Сравнение производительности (поиск 'a') ===");
        System.out.println("Размер\tcharCount()\tcharCount2()\tСоотношение\tБыстрее");
        System.out.println("--------------------------------------------------------");

        for (int size : sizes) {
            String testString = generateRandomString(size, size);

            // Прогрев
            for (int i = 0; i < 5; i++) {
                Main.charCount(testString, searchChar);
                Main.charCount2(testString, searchChar);
            }

            long countTime = measureTime(() -> Main.charCount(testString, searchChar), 10);
            long count2Time = measureTime(() -> Main.charCount2(testString, searchChar), 10);

            double ratio = (double) count2Time / countTime;
            String faster = ratio < 1 ? "charCount2()" : "charCount()";

            System.out.printf(Locale.US, "%d\t%d\t\t%d\t\t%.2f\t%s%n",
                    size, countTime, count2Time, ratio, faster);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Тест с разными искомыми символами")
    void testDifferentSearchChars() {
        System.out.println("\n=== Тест с разными искомыми символами ===");

        String testString = generateRandomString(1_000_000, 999);
        char[] searchChars = {'a', 'z', 'A', 'Z', '1', '@', ' '};

        System.out.println("Символ\tcharCount()\tcharCount2()\tСоотношение");
        System.out.println("------------------------------------------------");

        for (char ch : searchChars) {
            // Прогрев
            for (int i = 0; i < 3; i++) {
                Main.charCount(testString, ch);
                Main.charCount2(testString, ch);
            }

            long countTime = measureTime(() -> Main.charCount(testString, ch), 5);
            long count2Time = measureTime(() -> Main.charCount2(testString, ch), 5);

            double ratio = (double) count2Time / countTime;

            System.out.printf(Locale.US, "'%c'\t%d\t\t%d\t\t%.2f%n",
                    ch, countTime, count2Time, ratio);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Лучший и худший случаи")
    void testBestAndWorstCases() {
        System.out.println("\n=== Лучший и худший случаи ===");

        char searchChar = 'a';

        // Лучший случай - символ встречается часто
        System.out.println("\nЛучший случай (все символы 'a'):");
        measureAndPrint("charCount", () -> Main.charCount(repeatedCharString, searchChar));
        measureAndPrint("charCount2", () -> Main.charCount2(repeatedCharString, searchChar));

        // Худший случай - символ не встречается
        System.out.println("\nХудший случай (нет совпадений):");
        measureAndPrint("charCount", () -> Main.charCount(noMatchesString, searchChar));
        measureAndPrint("charCount2", () -> Main.charCount2(noMatchesString, searchChar));

        // Смешанный случай
        System.out.println("\nСмешанный случай (random):");
        String mixed = generateRandomString(1_000_000, 777);
        measureAndPrint("charCount", () -> Main.charCount(mixed, searchChar));
        measureAndPrint("charCount2", () -> Main.charCount2(mixed, searchChar));
    }

    private void measureAndPrint(String name, Runnable task) {
        long time = measureTime(task, 5);
        System.out.printf("%s: %d ns%n", name, time);
    }

    @Test
    @Order(6)
    @DisplayName("Измерение использования памяти")
    void testMemoryUsage() {
        System.out.println("\n=== Измерение использования памяти ===");

        String testString = generateRandomString(1_000_000, 999);
        char searchChar = 'a';

        // Очищаем память
        System.gc();
        sleep(100);

        // Измеряем для charCount()
        long memory1 = measureMemoryUsage(() -> Main.charCount(testString, searchChar));

        // Измеряем для charCount2()
        long memory2 = measureMemoryUsage(() -> Main.charCount2(testString, searchChar));

        System.out.printf("charCount()  использует память: %d bytes (%.2f KB)%n",
                memory1, memory1 / 1024.0);
        System.out.printf("charCount2() использует память: %d bytes (%.2f KB)%n",
                memory2, memory2 / 1024.0);
        System.out.printf("Разница: %.2f%%%n", 100.0 * (memory1 - memory2) / memory1);
    }

    @Test
    @Order(7)
    @DisplayName("Многопоточная производительность")
    @EnabledIfSystemProperty(named = "performance.test", matches = "true")
    void testConcurrentPerformance() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Многопоточная производительность ===");

        String testString = generateRandomString(100_000, 777);
        char searchChar = 'a';
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        System.out.println("Количество потоков: " + threads);

        // Тест для charCount()
        long countTime = measureConcurrent(executor, threads,
                () -> Main.charCount(testString, searchChar));

        // Тест для charCount2()
        long count2Time = measureConcurrent(executor, threads,
                () -> Main.charCount2(testString, searchChar));

        System.out.printf("charCount()  среднее в %d потоков: %d ns%n", threads, countTime);
        System.out.printf("charCount2() среднее в %d потоков: %d ns%n", threads, count2Time);
        System.out.printf("Соотношение: %.2f%n", (double) count2Time / countTime);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    @Order(8)
    @DisplayName("Тест масштабирования")
    void testScaling() {
        System.out.println("\n=== Тест масштабирования ===");

        int[] sizes = {1_000, 10_000, 100_000, 1_000_000};
        char searchChar = 'a';

        System.out.println("Размер\tcharCount() (ns/char)\tcharCount2() (ns/char)");
        System.out.println("----------------------------------------");

        for (int size : sizes) {
            String testString = generateRandomString(size, size);

            // Прогрев
            for (int w = 0; w < 3; w++) {
                Main.charCount(testString, searchChar);
                Main.charCount2(testString, searchChar);
            }

            long countTime = measureTime(() -> Main.charCount(testString, searchChar), 5);
            long count2Time = measureTime(() -> Main.charCount2(testString, searchChar), 5);

            System.out.printf(Locale.US, "%d\t%.2f\t\t\t%.2f%n",
                    size, (double) countTime / size,
                    (double) count2Time / size);
        }
    }

    @Test
    @Order(9)
    @DisplayName("Тест кэш-эффектов")
    void testCacheEffects() {
        System.out.println("\n=== Тест кэш-эффектов ===");

        int[] sizes = {100, 500, 1_000, 5_000, 10_000, 50_000, 100_000, 500_000, 1_000_000};
        char searchChar = 'a';

        System.out.println("Размер\tcharCount() (ns/char)\tcharCount2() (ns/char)");
        System.out.println("----------------------------------------");

        for (int size : sizes) {
            String testString = generateRandomString(size, size);

            // Прогрев
            for (int w = 0; w < 3; w++) {
                Main.charCount(testString, searchChar);
                Main.charCount2(testString, searchChar);
            }

            long countTime = measureTime(() -> Main.charCount(testString, searchChar), 5);
            long count2Time = measureTime(() -> Main.charCount2(testString, searchChar), 5);

            System.out.printf(Locale.US, "%d\t%.2f\t\t\t%.2f%n",
                    size, (double) countTime / size,
                    (double) count2Time / size);
        }
    }

    @Test
    @Order(10)
    @DisplayName("Тест на большом количестве итераций")
    void testManyIterations() {
        System.out.println("\n=== Тест на 1,000,000 итераций ===");

        String testString = "hello";
        char searchChar = 'l';
        AtomicLong total1 = new AtomicLong();
        AtomicLong total2 = new AtomicLong();

        long start = System.nanoTime();
        IntStream.range(0, 1_000_000).parallel().forEach(i -> {
            total1.addAndGet(Main.charCount(testString, searchChar));
        });
        long time1 = System.nanoTime() - start;

        start = System.nanoTime();
        IntStream.range(0, 1_000_000).parallel().forEach(i -> {
            total2.addAndGet(Main.charCount2(testString, searchChar));
        });
        long time2 = System.nanoTime() - start;

        System.out.printf("charCount():  %d ns (всего: %d)%n", time1, total1.get());
        System.out.printf("charCount2(): %d ns (всего: %d)%n", time2, total2.get());
        System.out.printf("Соотношение: %.2f%n", (double) time2 / time1);

        assertEquals(total1.get(), total2.get());
    }

    // Вспомогательные методы
    private long measureTime(Runnable task, int iterations) {
        long total = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            task.run();
            total += System.nanoTime() - start;
        }
        return total / iterations;
    }

    private long measureMemoryUsage(Runnable task) {
        Runtime runtime = Runtime.getRuntime();

        runtime.gc();
        sleep(50);

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        task.run();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        return Math.abs(memoryAfter - memoryBefore);
    }

    private double calculateStdDev(List<Long> values) {
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    private long measureConcurrent(ExecutorService executor, int threads, Runnable task)
            throws InterruptedException, ExecutionException {

        List<Callable<Long>> callables = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            callables.add(() -> {
                long start = System.nanoTime();
                task.run();
                return System.nanoTime() - start;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(callables);

        long total = 0;
        for (Future<Long> f : futures) {
            total += f.get();
        }

        return total / threads;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        System.out.println("✓ Тест завершен");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("\n=== Все тесты завершены ===");
    }
}