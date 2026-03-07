package com.klzw.common.map;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MineguardCommonMapApplication.class)
@ActiveProfiles("test")
@Tag("integration")
public abstract class AbstractMapIntegrationTest {
}
