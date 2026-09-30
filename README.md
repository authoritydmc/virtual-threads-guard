# virtual-threads-guard

> 🧵 Java 21+ Project Loom Carrier Thread Pinning Detector and JUnit 5 Diagnostic Guard.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

---

## 💡 Why `virtual-threads-guard`?

Java 21 Virtual Threads (Project Loom) provide massive concurrency, but **Carrier Thread Pinning** occurs when blocking I/O or sleep is executed inside `synchronized` blocks or native methods.

When a virtual thread pins its carrier thread, it monopolizes an underlying OS thread, **silently crippling throughput** without throwing exceptions.

**`virtual-threads-guard` provides:**
- 🚨 **JUnit 5 `@FailOnVirtualThreadPinning`:** Automatically fails CI/CD test suites if any legacy code, JDBC driver, or library pins a carrier thread.
- 🔍 **Stack Trace Reporting:** Prints the exact class, method, and line number where pinning occurred.
- ⚡️ **Zero-Overhead JFR Integration:** Uses low-overhead native JDK Flight Recorder events (`jdk.VirtualThreadPinned`).

---

## 📦 Maven Installation

```xml
<dependency>
    <groupId>io.github.authoritydmc</groupId>
    <artifactId>virtual-threads-guard</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

---

## 🚀 Usage

### 1. JUnit 5 Test Annotation

```java
import io.github.authoritydmc.loom.FailOnVirtualThreadPinning;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    @Test
    @FailOnVirtualThreadPinning // Fails test if any synchronized block pins a carrier thread!
    void testConcurrentOrderProcessing() {
        // Runs concurrent virtual thread workflow
    }
}
```

### 2. Programmatic Execution

```java
import io.github.authoritydmc.loom.VirtualThreadsGuard;

VirtualThreadsGuard.runGuarded(() -> {
    // Code executed inside virtual thread with real-time pinning assertion
});
```

---

## 📄 License

MIT © Raj Dubey
