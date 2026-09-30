package io.github.authoritydmc.loom;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingStream;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualThreadsGuard implements AutoCloseable {

    private final RecordingStream recordingStream;
    private final List<PinningEvent> capturedEvents = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public VirtualThreadsGuard() {
        this.recordingStream = new RecordingStream();
        this.recordingStream.enable("jdk.VirtualThreadPinned")
                .withThreshold(Duration.ofMillis(1));

        this.recordingStream.onEvent("jdk.VirtualThreadPinned", this::handleEvent);
    }

    private void handleEvent(RecordedEvent event) {
        String threadName = event.getThread() != null ? event.getThread().getJavaName() : "unknown";
        long durationNanos = event.getDuration().toNanos();
        String carrier = event.getString("carrierThread") != null ? event.getString("carrierThread") : "ForkJoinPool-worker";

        StackTraceElement[] elements = null;
        RecordedStackTrace st = event.getStackTrace();
        if (st != null) {
            elements = st.getFrames().stream()
                    .map(f -> new StackTraceElement(
                            f.getMethod().getType().getName(),
                            f.getMethod().getName(),
                            null,
                            f.getLineNumber()
                    ))
                    .toArray(StackTraceElement[]::new);
        }

        PinningEvent pe = new PinningEvent(threadName, durationNanos, carrier, elements, Instant.now());
        capturedEvents.add(pe);
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            recordingStream.startAsync();
        }
    }

    public List<PinningEvent> getCapturedEvents() {
        return Collections.unmodifiableList(new ArrayList<>(capturedEvents));
    }

    public boolean hasPinnedEvents() {
        return !capturedEvents.isEmpty();
    }

    public void clear() {
        capturedEvents.clear();
    }

    @Override
    public void close() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                recordingStream.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Executes a runnable on a virtual thread and asserts no carrier pinning occurs.
     */
    public static void runGuarded(Runnable task) {
        try (VirtualThreadsGuard guard = new VirtualThreadsGuard()) {
            guard.start();
            Thread vThread = Thread.ofVirtual().start(task);
            vThread.join();

            // Allow brief window for JFR buffer flush
            Thread.sleep(50);

            if (guard.hasPinnedEvents()) {
                StringBuilder errorMsg = new StringBuilder("Carrier thread pinning detected during virtual thread execution!\n");
                for (PinningEvent pe : guard.getCapturedEvents()) {
                    errorMsg.append(pe.getFormattedDetails()).append("\n");
                }
                throw new AssertionError(errorMsg.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
