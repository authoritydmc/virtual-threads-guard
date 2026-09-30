package io.github.authoritydmc.loom;

import java.time.Instant;

public record PinningEvent(
        String threadName,
        long durationNanos,
        String carrierThreadName,
        StackTraceElement[] stackTrace,
        Instant timestamp
) {
    public String getFormattedDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Virtual Thread '").append(threadName).append("' pinned to carrier '")
          .append(carrierThreadName).append("' for ").append(durationNanos / 1_000_000).append("ms.\n");

        if (stackTrace != null && stackTrace.length > 0) {
            sb.append("  Pinning Stack Trace:\n");
            for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                sb.append("    at ").append(stackTrace[i]).append("\n");
            }
        }
        return sb.toString();
    }
}
