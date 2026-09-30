package io.github.authoritydmc.loom;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JUnit 5 test method or class to fail if any Virtual Thread pins its carrier OS thread
 * (e.g. executing blocking operations inside synchronized blocks).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(VirtualThreadsGuardExtension.class)
public @interface FailOnVirtualThreadPinning {

    /**
     * Minimum duration threshold in milliseconds to consider an event pinning.
     * Default is 1ms.
     */
    long thresholdMs() default 1L;
}
