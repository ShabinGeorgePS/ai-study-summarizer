package com.shabin.aistudysummarizer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=testsecretkeytestsecretkeytest",
        "jwt.expiration=1000"
})
class AistudysummarizerApplicationTests {

    @Test
    void contextLoads() {
    }
}
