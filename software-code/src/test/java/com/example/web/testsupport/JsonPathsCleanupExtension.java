package com.example.web.testsupport;

import com.example.web.util.JsonPaths;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * 每个测试结束后清空 {@link JsonPaths} 的静态配置缓存，避免用例之间相互影响。
 */
public class JsonPathsCleanupExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        JsonPaths.clearSettingsCache();
    }
}
