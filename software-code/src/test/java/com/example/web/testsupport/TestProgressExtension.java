package com.example.web.testsupport;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 打印中文通过提示，并让 IDE / Test Runner 能捕获标准输出。
 */
public class TestProgressExtension
        implements BeforeAllCallback, BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(TestProgressExtension.class.getName());
    private static final String CLASS_FAILED = "classHasFailure";

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getStore(NS).put(CLASS_FAILED, new AtomicBoolean(false));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (context.getTestMethod().isEmpty()) {
            return;
        }
        System.out.println("[JUnit] 运行: " + context.getDisplayName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getTestMethod().isEmpty()) {
            return;
        }
        Throwable ex = context.getExecutionException().orElse(null);
        ExtensionContext classCtx = classContextForMethod(context);
        if (classCtx != null && ex != null) {
            AtomicBoolean failed = classCtx.getStore(NS).get(CLASS_FAILED, AtomicBoolean.class);
            if (failed != null) {
                failed.set(true);
            }
        }
        if (ex == null) {
            System.out.println("[JUnit] 通过 — 本条测试没有问题: " + context.getDisplayName());
        } else {
            System.out.println("[JUnit] 失败: " + context.getDisplayName());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        AtomicBoolean failed = context.getStore(NS).get(CLASS_FAILED, AtomicBoolean.class);
        if (failed == null) {
            return;
        }
        String name = context.getTestClass().map(Class::getSimpleName).orElse(context.getDisplayName());
        if (!failed.get()) {
            System.out.println("[JUnit] 全部通过 — 本测试类没有问题: " + name);
        } else {
            System.out.println("[JUnit] 未全部通过 — 请查看上方失败信息: " + name);
        }
    }

    private static ExtensionContext classContextForMethod(ExtensionContext methodContext) {
        ExtensionContext c = methodContext.getParent().orElse(null);
        while (c != null) {
            if (c.getTestClass().isPresent() && c.getTestMethod().isEmpty()) {
                return c;
            }
            c = c.getParent().orElse(null);
        }
        return null;
    }
}
