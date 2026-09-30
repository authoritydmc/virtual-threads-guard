package io.github.authoritydmc.loom;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class VirtualThreadsGuardExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(VirtualThreadsGuardExtension.class);

    private static final String GUARD_KEY = "guard";

    @Override
    public void beforeEach(ExtensionContext context) {
        VirtualThreadsGuard guard = new VirtualThreadsGuard();
        guard.start();
        context.getStore(NAMESPACE).put(GUARD_KEY, guard);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        VirtualThreadsGuard guard = context.getStore(NAMESPACE).remove(GUARD_KEY, VirtualThreadsGuard.class);
        if (guard != null) {
            // Brief wait for JFR stream events
            Thread.sleep(50);
            List<PinningEvent> events = guard.getCapturedEvents();
            guard.close();

            if (!events.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Test failed due to ").append(events.size())
                  .append(" Virtual Thread Carrier Pinning event(s):\n");
                for (PinningEvent event : events) {
                    sb.append(event.getFormattedDetails()).append("\n");
                }
                throw new AssertionError(sb.toString());
            }
        }
    }
}
