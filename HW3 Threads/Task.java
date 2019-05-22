import java.util.concurrent.Callable;

public class Task<T> {
    private final Callable<? extends T> callable;
    private volatile boolean ready;
    private boolean success;
    private T result;
    private Exception error;

    public Task(Callable<? extends T> callable) {
        this.callable = callable;
    }

    public T get() {
        if (!ready) {
            synchronized (this) {
                if (!ready) {
                    try {
                        result = callable.call();
                        success = true;
                    } catch (Exception e) {
                        error = e;
                    }
                    ready = true;
                }
            }
        }

        if (success) {
            return result;
        } else {
            throw new TaskFailedException(error);
        }
    }
}