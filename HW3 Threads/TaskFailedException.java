public class TaskFailedException extends RuntimeException {
    public final Exception exception;
    public TaskFailedException(Exception exception) {
        this.exception = exception;
    }
}
