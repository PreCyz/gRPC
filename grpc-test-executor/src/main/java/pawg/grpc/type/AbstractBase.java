package pawg.grpc.type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractBase implements Runnable {
    protected final List<Duration> millis;
    protected final int numberOfCalls;
    protected final int numberOfRecords;
    protected final String username;

    protected int responsePayloadSize;
    protected int requestPayloadSize;

    protected AbstractBase(int numberOfCalls, int numberOfRecords, String username) {
        this.numberOfCalls = numberOfCalls;
        this.numberOfRecords = numberOfRecords;
        this.username = username;
        this.millis = new ArrayList<>();
    }

    protected abstract void execute(int requestNumber);
    public abstract int requestPayloadSize();
    public abstract int responsePayloadSize();

    @Override
    public void run() {
        for (int i = 1; i <= numberOfCalls; i++) {
            LocalDateTime start = LocalDateTime.now();
            execute(i);
            millis.add(Duration.between(start, LocalDateTime.now()));
        }
    }

    public List<Duration> getMillis() {
        return Collections.unmodifiableList(millis);
    }

    public int getResponsePayloadSize() {
        return responsePayloadSize;
    }

    public int getRequestPayloadSize() {
        return requestPayloadSize;
    }
}
