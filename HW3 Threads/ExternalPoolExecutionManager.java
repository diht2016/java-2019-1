public class ExternalPoolExecutionManager implements ExecutionManager {
    private final ThreadPool pool;

    public ExternalPoolExecutionManager(ThreadPool pool) {
        this.pool = pool;
        pool.start();
    }

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {
        InnerContext context = new InnerContext(tasks.length);
        for (Runnable task : tasks) {
            pool.execute(() -> {
                synchronized (context) {
                    if (context.availableTaskCount == 0) {
                        return;
                    }
                    context.availableTaskCount--;
                }

                try {
                    task.run();
                    synchronized (context) {
                        context.completedTaskCount++;
                        context.unfinishedTaskCount--;
                    }
                } catch (Exception e) {
                    synchronized (context) {
                        context.failedTaskCount++;
                        context.unfinishedTaskCount--;
                    }
                }
            });
        }
        return context;
    }

    private static class InnerContext implements Context {
        private int availableTaskCount;
        private int unfinishedTaskCount;
        private int completedTaskCount;
        private int failedTaskCount;
        private int interruptedTaskCount;

        private InnerContext(int count) {
            unfinishedTaskCount = count;
            availableTaskCount = count;
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
            interruptedTaskCount = availableTaskCount;
            unfinishedTaskCount -= interruptedTaskCount;
            availableTaskCount = 0;
        }

        @Override
        public boolean isFinished() {
            return unfinishedTaskCount == 0;
        }
    }
}
