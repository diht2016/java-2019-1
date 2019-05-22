import java.util.LinkedList;
import java.util.Queue;

public class ScalableThreadPool implements ThreadPool {
    private final Queue<Runnable> tasks = new LinkedList<>();
    private final int min;
    private final int max;
    private int jobs;
    private int running;

    public ScalableThreadPool(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void start() {
        running = min;
        for (int i = 0; i < min; i++) {
            new Thread(this::consume).start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (tasks) {
            tasks.add(runnable);
            jobs++;
            if (running < jobs && running < max) {
                new Thread(this::consume).start();
                running++;
            }
        }
        tasks.notify();
    }

    private void consume() {
        while (true) {
            Runnable task;
            synchronized (tasks) {
                while (tasks.isEmpty()) {
                    if (running > min) {
                        running--;
                        return;
                    }
                    try {
                        tasks.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                task = tasks.remove();
            }
            task.run();
            synchronized (tasks) {
                jobs--;
            }
        }
    }
}
