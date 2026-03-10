package com.klzw.common.map;

import org.junit.jupiter.api.Tag;
import com.klzw.common.core.config.DotenvInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = MineguardCommonMapApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = DotenvInitializer.class)
@Tag("integration")
public abstract class AbstractMapIntegrationTest {
}
