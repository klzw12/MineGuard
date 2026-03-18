package com.klzw.service.user;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.user.config.TestMockConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@Tag("integration")
@SpringBootTest(classes = MineguardUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
@Import(TestMockConfig.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
public abstract class AbstractIntegrationTest {
}
