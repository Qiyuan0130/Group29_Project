package com.example.web.util;

import com.example.web.testsupport.JsonPathsCleanupExtension;
import com.example.web.testsupport.TestProgressExtension;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith({
        MockitoExtension.class,
        TestProgressExtension.class,
        JsonPathsCleanupExtension.class
})
class JsonPathsTest {

    @Mock
    private ServletContext servletContext;

    @Test
    void dataDirectory_usesDefaultRelativeToWebappRoot(@TempDir Path webappRoot) throws Exception {
        when(servletContext.getResourceAsStream("/WEB-INF/app-settings.properties")).thenReturn(null);
        when(servletContext.getRealPath("/")).thenReturn(webappRoot.toString());

        Path data = JsonPaths.dataDirectory(servletContext);

        assertEquals(webappRoot.resolve("WEB-INF").resolve("data").normalize(), data.normalize());
    }

    @Test
    void dataDirectory_respectsAbsolutePathInSettings(@TempDir Path webappRoot, @TempDir Path customData) throws Exception {
        String props = "dataDir=" + customData.toString().replace('\\', '/') + "\n";
        ByteArrayInputStream in = new ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8));
        when(servletContext.getResourceAsStream("/WEB-INF/app-settings.properties")).thenReturn(in);
        when(servletContext.getRealPath("/")).thenReturn(webappRoot.toString());

        Path data = JsonPaths.dataDirectory(servletContext);

        assertEquals(customData.normalize(), data.normalize());
    }

    @Test
    void jsonFile_resolvesUnderDataDirectory(@TempDir Path webappRoot) {
        when(servletContext.getResourceAsStream("/WEB-INF/app-settings.properties")).thenReturn(null);
        when(servletContext.getRealPath("/")).thenReturn(webappRoot.toString());

        Path file = JsonPaths.jsonFile(servletContext, "users.json");

        assertTrue(file.toString().replace('\\', '/').endsWith("WEB-INF/data/users.json"));
    }
}
