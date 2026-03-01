package ture;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainTest {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int MEASUREMENT_ITERATIONS = 100_000;

    private String smallString;
    private String mediumString;
    private String largeString;
    private String hugeString;
    private String noSpacesString;
    private String manySpacesString;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Подготовка тестовых данных ===");

        smallString = generateString(100, 0.3);      // 100 символов, 30% пробелов
        mediumString = generateString(1_000, 0.3);    // 1K символов
        largeString = generateString(100_000, 0.3);   // 100K символов
        hugeString = generateString(1_000_000, 0.3);  // 1M символов

        noSpacesString = "abcdefghijklmnopqrstuvwxyz".repeat(1000);
        manySpacesString = " ".repeat(1_000_000);

        // Проверяем корректность
        assertEquals(Main.clean(smallString), Main.clean2(smallString));
        System.out.println("✓ Методы дают одинаковые результаты");
    }

    private String generateString(int length, double spaceProbability) {
        Random random = new Random(42);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (random.nextDouble() < spaceProbability) {
                sb.append(' ');
            } else {
                sb.append((char) ('a' + random.nextInt(26)));
            }
        }
        return sb.toString();
    }

    @Test
    @Order(0)
    @DisplayName("Базовые тесты корректности")
    void testBasicCorrectness() {
        System.out.println("Тестирование корректности");

        assertAll("Простые случаи",
                () -> assertEquals("", Main.clean("")),
                () -> assertEquals("", Main.clean2("")),
                () -> assertEquals("", Main.clean("   ")),
                () -> assertEquals("", Main.clean2("   ")),
                () -> assertEquals("hello", Main.clean("hello")),
                () -> assertEquals("hello", Main.clean2("hello")),
                () -> assertEquals("hello", Main.clean(" hello")),
                () -> assertEquals("hello", Main.clean2(" hello")),
                () -> assertEquals("hello", Main.clean("hello ")),
                () -> assertEquals("hello", Main.clean2("hello ")),
                () -> assertEquals("helloworld", Main.clean("hello world")),
                () -> assertEquals("helloworld", Main.clean2("hello world"))
        );

        System.out.println("✓ Все базовые тесты пройдены");
    }

    @ParameterizedTest
    @Order(1)
    @CsvSource({
            "'', ''",
            "' ', ''",
            "'hello', 'hello'",
            "' hello world ', 'helloworld'",
            "'a b c d e', 'abcde'",
            "'   a   b   c   ', 'abc'",
            "'! @ # $', '!@#$'",
            "'123 456 789', '123456789'"
    })
    @DisplayName("Параметризованный тест корректности")
    void testParameterizedCorrectness(String input, String expected) {
        assertEquals(expected, Main.clean(input));
        assertEquals(expected, Main.clean2(input));
    }

    @Test
    @Order(2)
    @DisplayName("Тест стабильности с прогревом")
    void testStability() {
        System.out.println("\n=== Тест стабильности ===");

        // Прогрев JIT
        System.out.print("Прогрев JIT");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Main.clean(mediumString);
            Main.clean2(mediumString);
            if (i % 2000 == 0) System.out.print(".");
        }
        System.out.println(" готово!");

        // Измерения
        List<Long> cleanTimes = new ArrayList<>();
        List<Long> clean2Times = new ArrayList<>();

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            cleanTimes.add(measureTime(() -> Main.clean(mediumString)));
            clean2Times.add(measureTime(() -> Main.clean2(mediumString)));
        }

        // Статистика
        printStats("replace()", cleanTimes);
        printStats("StringBuilder", clean2Times);
    }

    private void printStats(String name, List<Long> times) {
        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        double stdDev = calculateStdDev(times);
        long min = Collections.min(times);
        long max = Collections.max(times);

        System.out.printf("%s:\n", name);
        System.out.printf(Locale.US, "  Среднее: %.2f ns\n", avg);
        System.out.printf(Locale.US, "  Стд.откл.: %.2f (%.1f%%)\n", stdDev, (stdDev / avg * 100));
        System.out.printf(Locale.US, "  Мин/Макс: %d / %d ns\n", min, max);
        System.out.printf(Locale.US, "  Медиана: %d ns\n", calculateMedian(times));
    }

    @Test
    @Order(3)
    @DisplayName("Сравнение производительности на разных размерах")
    void testPerformanceScaling() {
        System.out.println("\n=== Сравнение производительности ===");

        int[] sizes = {100, 1_000, 10_000, 100_000, 1_000_000};
        double[] spaceProbabilities = {0.0, 0.3, 0.7, 1.0};

        System.out.println("Размер\tПробелы\treplace() (ns)\tStringBuilder (ns)\tСоотношение\tБыстрее");
        System.out.println("--------------------------------------------------------------------------------");

        for (int size : sizes) {
            for (double prob : spaceProbabilities) {
                String testString = generateString(size, prob);

                // Прогрев
                for (int i = 0; i < 1000; i++) {
                    Main.clean(testString);
                    Main.clean2(testString);
                }

                long time1 = measureTime(() -> Main.clean(testString), 100);
                long time2 = measureTime(() -> Main.clean2(testString), 100);

                double ratio = (double) time2 / time1;
                String faster = ratio < 1 ? "StringBuilder" : "replace()";

                System.out.printf(Locale.US, "%d\t%.0f%%\t%d\t\t%d\t\t%.2f\t%s%n",
                        size, prob * 100, time1, time2, ratio, faster);
            }
            System.out.println("--------------------------------------------------------------------------------");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Лучший и худший случаи")
    void testBestAndWorstCases() {
        System.out.println("\n=== Лучший и худший случаи ===");

        // Лучший случай для replace() - нет пробелов
        System.out.println("\nЛучший случай (нет пробелов):");
        measureAndPrint("replace()", () -> Main.clean(noSpacesString));
        measureAndPrint("StringBuilder", () -> Main.clean2(noSpacesString));

        // Худший случай для replace() - все пробелы
        System.out.println("\nХудший случай (все пробелы):");
        measureAndPrint("replace()", () -> Main.clean(manySpacesString));
        measureAndPrint("StringBuilder", () -> Main.clean2(manySpacesString));

        // Средний случай
        System.out.println("\nСредний случай (30% пробелов):");
        measureAndPrint("replace()", () -> Main.clean(mediumString));
        measureAndPrint("StringBuilder", () -> Main.clean2(mediumString));
    }

    private void measureAndPrint(String name, Runnable task) {
        long time = measureTime(task, 100);
        System.out.printf(Locale.US, "  %s: %d ns%n", name, time);
    }

    @Test
    @Order(5)
    @DisplayName("Тест использования памяти")
    void testMemoryUsage() {
        System.out.println("\n=== Тест использования памяти ===");

        String testString = generateString(1_000_000, 0.3);

        // Очищаем память
        System.gc();
        sleep(100);

        // Измеряем для replace()
        long memory1 = measureMemoryUsage(() -> Main.clean(testString));

        // Измеряем для StringBuilder
        long memory2 = measureMemoryUsage(() -> Main.clean2(testString));

        System.out.printf("replace() использует: %d bytes (%.2f KB)%n", memory1, memory1 / 1024.0);
        System.out.printf("StringBuilder использует: %d bytes (%.2f KB)%n", memory2, memory2 / 1024.0);
        System.out.printf("Разница: %.2f%%%n", 100.0 * (memory1 - memory2) / memory1);
    }

    @Test
    @Order(6)
    @DisplayName("Многопоточный тест")
    void testConcurrentPerformance() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Многопоточный тест ===");

        String testString = generateString(100_000, 0.3);
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        System.out.println("Количество потоков: " + threads);

        // Тест для replace()
        long time1 = measureConcurrent(executor, threads,
                () -> Main.clean(testString));

        // Тест для StringBuilder
        long time2 = measureConcurrent(executor, threads,
                () -> Main.clean2(testString));

        System.out.printf(Locale.US, "replace() среднее: %d ns%n", time1);
        System.out.printf(Locale.US, "StringBuilder среднее: %d ns%n", time2);
        System.out.printf(Locale.US, "Соотношение: %.2f%n", (double) time2 / time1);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    @Order(7)
    @DisplayName("Тест с разным содержанием пробелов")
    void testDifferentSpaceDensities() {
        System.out.println("\n=== Тест с разной плотностью пробелов ===");

        int size = 1_000_000;
        double[] densities = {0.0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0};

        System.out.println("Плотность\treplace() (ns)\tStringBuilder (ns)\tСоотношение");
        System.out.println("--------------------------------------------------------");

        for (double density : densities) {
            String testString = generateString(size, density);

            long time1 = measureTime(() -> Main.clean(testString), 10);
            long time2 = measureTime(() -> Main.clean2(testString), 10);

            double ratio = (double) time2 / time1;

            System.out.printf(Locale.US, "%.0f%%\t\t%d\t\t%d\t\t%.2f%n",
                    density * 100, time1, time2, ratio);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Тест на очень длинных строках")
    void testVeryLongStrings() {
        System.out.println("\n=== Тест на очень длинных строках ===");

        int[] sizes = {10_000_000, 20_000_000, 50_000_000};

        for (int size : sizes) {
            if (size > 10_000_000) {
                System.out.printf("\nПропуск теста с %d символов (слишком большой для текущей среды)%n", size);
                continue;
            }

            System.out.printf("\nТест с %d символов:%n", size);
            String testString = generateString(size, 0.3);

            long time1 = measureTime(() -> Main.clean(testString), 1);
            long time2 = measureTime(() -> Main.clean2(testString), 1);

            System.out.printf(Locale.US, "replace(): %d ns (%.2f ms)%n", time1, time1 / 1_000_000.0);
            System.out.printf(Locale.US, "StringBuilder: %d ns (%.2f ms)%n", time2, time2 / 1_000_000.0);
            System.out.printf(Locale.US, "Соотношение: %.2f%n", (double) time2 / time1);
        }
    }

    @Test
    @Order(9)
    @DisplayName("Анализ времени на символ")
    void testTimePerCharacter() {
        System.out.println("\n=== Анализ времени на символ ===");

        int[] sizes = {1000, 10000, 100000, 1000000};

        System.out.println("Размер\treplace() (ns/char)\tStringBuilder (ns/char)");
        System.out.println("------------------------------------------------");

        for (int size : sizes) {
            String testString = generateString(size, 0.3);

            long time1 = measureTime(() -> Main.clean(testString), 100);
            long time2 = measureTime(() -> Main.clean2(testString), 100);

            System.out.printf(Locale.US, "%d\t%.3f\t\t\t%.3f%n",
                    size, (double) time1 / size, (double) time2 / size);
        }
    }

    // Вспомогательные методы
    private long measureTime(Runnable task) {
        return measureTime(task, 1);
    }

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

    private long calculateMedian(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
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
}