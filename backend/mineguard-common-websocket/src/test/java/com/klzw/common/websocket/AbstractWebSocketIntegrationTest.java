package com.klzw.common.websocket;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = WebSocketTestApplication.class)
@ActiveProfiles("test")
@Tag("integration")
public abstract class AbstractWebSocketIntegrationTest {
}
