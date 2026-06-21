import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an operation that may potentially fail with an exception
 */
@FunctionalInterface
interface ThrowableOperation<T> {
    T execute() throws Throwable;
}

/**
 * Represents the result of a computation that could have succeeded with a value of the type T
 * or failed with a Throwable.
 */
interface Try<T> {
    /**
     * Executes the given operation and returns the result wrapped in a Success or Failure
     */
    static <T> Try<T> of(ThrowableOperation<T> operation) {
        try {
            var value = operation.execute();
            return new Success<>(value);
        } catch (Throwable e) {
            return new Failure<>(e);
        }
    }

    /**
     * Returns true if the original operation succeeded, otherwise returns false
     */
    boolean isSuccess();

    /**
     * Returns the resulting value if this is a Success, otherwise throws the original exception
     */
    T get() throws Throwable;

    /**
     * Returns the resulting value if this is a Success,
     * otherwise throws the original exception wrapped in a RuntimeException
     *
     * @throws RuntimeException that wraps the original exception
     */
    T getUnchecked();

    /**
     * Converts this to a non-empty Optional that wraps the resulting value if this is Success,
     * otherwise returns an empty Optional
     */
    Optional<T> toOptional();
}


/**
 * Represents a successful execution
 */
class Success<T> implements Try<T> {
    private final T value;

    Success(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T getUnchecked() {
        return value;
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.of(value);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String toString() {
        return "Success[" + value + "]";
    }
}

/**
 * Represents a failed execution
 */
class Failure<T> implements Try<T> {
    private final Throwable e;

    Failure(Throwable e) {
        this.e = e;
    }

    @Override
    public T get() throws Throwable {
        throw e;
    }

    @Override
    public T getUnchecked() {
        throw new RuntimeException(e);
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String toString() {
        return "Failure[" + e + "]";
    }
}


public class Main {
    static void main() {
        {
            Try<Integer> tryParse = Try.of(() -> Integer.parseInt("23")); // Success[23]

            System.out.println(tryParse.isSuccess()); // true
            System.out.println(tryParse.getUnchecked()); // 23
            System.out.println(tryParse.toOptional().isEmpty()); // false
        }
        {
            Try<Integer> tryParse = Try.of(() -> Integer.parseInt("V23")); // Failure[java.lang.NumberFormatException: For input string: "V23"]

            System.out.println(tryParse.isSuccess()); // false
            System.out.println(tryParse.toOptional().isEmpty()); // true
            System.out.println(tryParse.getUnchecked()); // выбрасывает RuntimeException, оборачивающее исходное исключение
        }
        {
            int n = 100;
            Try<Integer> division = Try.of(() -> n / 0); // Failure[java.lang.ArithmeticException: / by zero]
        }
    }
}
