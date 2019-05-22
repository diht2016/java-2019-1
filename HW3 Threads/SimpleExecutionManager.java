import java.util.LinkedList;
import java.util.Queue;

public class SimpleExecutionManager implements ExecutionManager {
    private final Queue<Runnable> availableTasks = new LinkedList<>();
    private final Queue<InnerContext> availableContexts = new LinkedList<>();

    public SimpleExecutionManager(int count) {
        for (int i = 0; i < count; i++) {
            new Thread(this::consume).start();
        }
    }

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {
        InnerContext context = new InnerContext(tasks.length);
        synchronized (availableTasks) {
            for (Runnable task : availableTasks) {
                availableTasks.add(task);
                availableContexts.add(context);
            }
        }
        return context;
    }

    private void consume() {
        while (true) {
            Runnable task;
            InnerContext context;

            synchronized (availableTasks) {
                while (availableTasks.isEmpty()) {
                    try {
                        availableTasks.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                task = availableTasks.remove();
                context = availableContexts.remove();
            }

            synchronized (context) {
                if (context.isInterrupted) {
                    continue;
                }
            }

            try {
                task.run();
                synchronized (context) {
                    context.completedTaskCount++;
                }
            } catch (Exception e) {
                synchronized (context) {
                    context.failedTaskCount++;
                }
            }
        }
    }

    private static class InnerContext implements Context {
        private final int totalTaskCount;
        private int completedTaskCount;
        private int failedTaskCount;
        private int interruptedTaskCount;
        private boolean isInterrupted;

        private InnerContext(int count) {
            totalTaskCount = count;
        }

        @Override
        public synchronized int getCompletedTaskCount() {
            return completedTaskCount;
        }

        @Override
        public synchronized int getFailedTaskCount() {
            return failedTaskCount;
        }

        @Override
        public synchronized int getInterruptedTaskCount() {
            return interruptedTaskCount;
        }

        @Override
        public synchronized void interrupt() {
            interruptedTaskCount = totalTaskCount - completedTaskCount - failedTaskCount;
            isInterrupted = true;
        }

        @Override
        public boolean isFinished() {
            return completedTaskCount + failedTaskCount + interruptedTaskCount == totalTaskCount;
        }
    }
}
