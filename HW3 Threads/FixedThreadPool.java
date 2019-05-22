import java.util.LinkedList;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {
    private final Queue<Runnable> tasks = new LinkedList<>();
    private final int count;

    public FixedThreadPool(int count) {
        this.count = count;
    }

    @Override
    public void start() {
        for (int i = 0; i < count; i++) {
            new Thread(this::consume).start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (tasks) {
            tasks.add(runnable);
        }
        tasks.notify();
    }

    private void consume() {
        while (true) {
            Runnable task;
            synchronized (tasks) {
                while (tasks.isEmpty()) {
                    try {
                        tasks.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                task = tasks.remove();
            }
            task.run();
        }
    }
}
