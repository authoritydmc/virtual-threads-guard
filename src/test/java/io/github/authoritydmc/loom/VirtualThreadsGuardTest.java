package io.github.authoritydmc.loom;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualThreadsGuardTest {

    @Test
    void testNonPinningExecutionPasses() {
        AtomicBoolean ran = new AtomicBoolean(false);

        VirtualThreadsGuard.runGuarded(() -> {
            try {
                // Non-pinning sleep (unparks cleanly)
                Thread.sleep(10);
                ran.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertTrue(ran.get());
    }

    @Test
    @FailOnVirtualThreadPinning
    void testAnnotationGuardsCleanVirtualThread() throws InterruptedException {
        Thread vThread = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        });
        vThread.join();
    }
}
