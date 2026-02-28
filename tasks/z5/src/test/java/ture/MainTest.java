package ture;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainTest {

    private static final int WARMUP_ITERATIONS = 10;
    private static final int MEASUREMENT_ITERATIONS = 20;
    private String smallString;
    private String mediumString;
    private String largeString;
    private String hugeString;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("\n=== " + description.getMethodName() + " ===");
        }
    };

    @Before
    public void setUp() {
        System.out.println("Подготовка тестовых данных...");
        // Используем фиксированный seed для воспроизводимости
        smallString = generateRandomString(100, 42);
        mediumString = generateRandomString(1_000, 43);
        largeString = generateRandomString(100_000, 44);
        hugeString = generateRandomString(1_000_000, 45);

        // Проверяем что оба метода дают одинаковый результат
        Map<String, Long> result1 = Main.count(smallString);
        Map<String, Long> result2 = Main.count2(smallString);
        assertEquals("Методы должны давать одинаковый результат", result1, result2);

        System.out.printf("Строка 100: гласных=%d, согласных=%d%n",
                result1.get("Гласных"), result1.get("Согласных"));
    }

    private String generateRandomString(int length, int seed) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // Только буквы, без цифр
            char c = (char) ('a' + random.nextInt(26));
            if (random.nextBoolean()) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Test
    public void test1StabilityWithWarmup() {
        System.out.println("Тест стабильности с прогревом");

        // Прогревочные итерации
        System.out.print("Прогрев JIT компилятора");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Main.count(mediumString);
            Main.count2(mediumString);
            if (i % 5 == 0) System.out.print(".");
        }
        System.out.println(" готово!");

        // Измерения
        long[] countTimes = new long[MEASUREMENT_ITERATIONS];
        long[] count2Times = new long[MEASUREMENT_ITERATIONS];

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            countTimes[i] = measureTime(() -> Main.count(mediumString), 1);
            count2Times[i] = measureTime(() -> Main.count2(mediumString), 1);
        }

        // Сортируем и отбрасываем выбросы (убираем 10% самых быстрых и 10% самых медленных)
        Arrays.sort(countTimes);
        Arrays.sort(count2Times);

        int trimSize = MEASUREMENT_ITERATIONS / 10;
        long[] trimmedCount = Arrays.copyOfRange(countTimes, trimSize, MEASUREMENT_ITERATIONS - trimSize);
        long[] trimmedCount2 = Arrays.copyOfRange(count2Times, trimSize, MEASUREMENT_ITERATIONS - trimSize);

        double avgCount = Arrays.stream(trimmedCount).average().orElse(0);
        double avgCount2 = Arrays.stream(trimmedCount2).average().orElse(0);
        double stdDevCount = calculateStdDev(trimmedCount);
        double stdDevCount2 = calculateStdDev(trimmedCount2);

        System.out.printf("count()  среднее: %.0f ns, стд.откл.: %.0f (%.1f%%)%n",
                avgCount, stdDevCount, (stdDevCount / avgCount * 100));
        System.out.printf("count2() среднее: %.0f ns, стд.откл.: %.0f (%.1f%%)%n",
                avgCount2, stdDevCount2, (stdDevCount2 / avgCount2 * 100));

        // Относительное отклонение не должно превышать 30%
        assertTrue("Слишком большой разброс в count()", stdDevCount / avgCount < 0.3);
        assertTrue("Слишком большой разброс в count2()", stdDevCount2 / avgCount2 < 0.3);
    }

    @Test
    public void test2ComparePerformance() {
        int[] sizes = {100, 1_000, 10_000, 100_000, 1_000_000};

        System.out.println("\n=== Сравнение производительности ===");
        System.out.println("Размер\tcount() (ns)\tcount2() (ns)\tСоотношение\tБыстрее");
        System.out.println("--------------------------------------------------------");

        for (int size : sizes) {
            String testString = generateRandomString(size, size);

            // Прогрев
            for (int i = 0; i < 5; i++) {
                Main.count(testString);
                Main.count2(testString);
            }

            long countTime = measureTime(() -> Main.count(testString), 10);
            long count2Time = measureTime(() -> Main.count2(testString), 10);

            double ratio = (double) count2Time / countTime;
            String faster = ratio < 1 ? "count2()" : "count()";

            System.out.printf(Locale.US, "%d\t%d\t\t%d\t\t%.2f\t%s%n",
                    size, countTime, count2Time, ratio, faster);
        }
    }

    @Test
    public void test3DifferentRatios() {
        System.out.println("\n=== Тест с разным составом строк ===");

        // Строки с разным соотношением гласных/согласных
        String onlyVowels = "aeiou".repeat(20_000); // 100k символов
        String onlyConsonants = "bcdfghjklmnpqrstvwxyz".repeat(4_000); // ~100k символов
        String mixed = generateRandomString(100_000, 100);
        String withDigits = mixed + "1234567890".repeat(10_000); // добавляем цифры

        testCase("Только гласные", onlyVowels);
        testCase("Только согласные", onlyConsonants);
        testCase("Смешанные", mixed);
        testCase("С цифрами", withDigits);
    }

    private void testCase(String name, String testString) {
        // Проверяем что оба метода дают одинаковый результат
        Map<String, Long> result1 = Main.count(testString);
        Map<String, Long> result2 = Main.count2(testString);
        assertEquals(name + ": методы должны давать одинаковый результат", result1, result2);

        long countTime = measureTime(() -> Main.count(testString), 5);
        long count2Time = measureTime(() -> Main.count2(testString), 5);

        System.out.printf("%s:%n  count(): %d ns%n  count2(): %d ns%n  Гласных: %d, Согласных: %d%n",
                name, countTime, count2Time,
                result1.get("Гласных"), result1.get("Согласных"));
    }

    @Test
    public void test4MemoryUsage() {
        System.out.println("\n=== Измерение использования памяти ===");

        String testString = generateRandomString(1_000_000, 999);

        // Очищаем память
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Измеряем для count()
        long memory1 = measureMemoryUsage(() -> Main.count(testString));

        // Измеряем для count2()
        long memory2 = measureMemoryUsage(() -> Main.count2(testString));

        System.out.printf("count()  использует память: %d bytes (%.2f KB)%n",
                memory1, memory1 / 1024.0);
        System.out.printf("count2() использует память: %d bytes (%.2f KB)%n",
                memory2, memory2 / 1024.0);
        System.out.printf("Разница: %.2f%%%n", 100.0 * (memory1 - memory2) / memory1);
    }

    private long measureMemoryUsage(Runnable task) {
        Runtime runtime = Runtime.getRuntime();

        // Принудительный GC
        runtime.gc();
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        task.run();

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        return Math.abs(memoryAfter - memoryBefore);
    }

    @Test
    public void test5ConcurrentPerformance() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Многопоточная производительность ===");

        String testString = generateRandomString(100_000, 777);
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        System.out.println("Количество потоков: " + threads);

        // Тест для count()
        long countTime = measureConcurrent(executor, threads,
                () -> Main.count(testString));

        // Тест для count2()
        long count2Time = measureConcurrent(executor, threads,
                () -> Main.count2(testString));

        System.out.printf("count()  среднее в %d потоков: %d ns%n", threads, countTime);
        System.out.printf("count2() среднее в %d потоков: %d ns%n", threads, count2Time);
        System.out.printf("Соотношение: %.2f%n", (double) count2Time / countTime);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
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

    @Test
    public void test6ScalingLinear() {
        System.out.println("\n=== Проверка линейности масштабирования ===");

        int[] sizes = {1_000, 10_000, 100_000, 1_000_000};
        long[] countTimes = new long[sizes.length];
        long[] count2Times = new long[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            String testString = generateRandomString(sizes[i], sizes[i]);

            // Прогрев
            for (int w = 0; w < 3; w++) {
                Main.count(testString);
                Main.count2(testString);
            }

            countTimes[i] = measureTime(() -> Main.count(testString), 5);
            count2Times[i] = measureTime(() -> Main.count2(testString), 5);

            double ratioToPrev = i > 0 ?
                    (double) countTimes[i] / countTimes[i-1] / (sizes[i] / sizes[i-1]) : 1;

            System.out.printf("Размер %d: count()=%d ns (x%.2f от линейного), count2()=%d ns%n",
                    sizes[i], countTimes[i], ratioToPrev, count2Times[i]);
        }
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

    private double calculateStdDev(long[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    @Test
    public void test7CacheEffects() {
        System.out.println("\n=== Тест кэш-эффектов ===");

        // Тестируем с разными размерами чтобы увидеть влияние кэша
        int[] sizes = {100, 500, 1_000, 5_000, 10_000, 50_000, 100_000, 500_000, 1_000_000};

        System.out.println("Размер\tcount() (ns/char)\tcount2() (ns/char)");
        System.out.println("----------------------------------------");

        for (int size : sizes) {
            String testString = generateRandomString(size, size);

            // Прогрев
            for (int w = 0; w < 3; w++) {
                Main.count(testString);
                Main.count2(testString);
            }

            long countTime = measureTime(() -> Main.count(testString), 5);
            long count2Time = measureTime(() -> Main.count2(testString), 5);

            System.out.printf(Locale.US, "%d\t%.2f\t\t\t%.2f%n",
                    size, (double) countTime / size,
                    (double) count2Time / size);
        }
    }
}