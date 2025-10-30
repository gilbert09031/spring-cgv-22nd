package com.ceos22.cgv_clone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
		"spring.jwt.secret=test-secret-key-for-jwt-token-generation-and-validation-minimum-256-bits-test-secret-key-for-testing",
		"spring.jwt.token.access-expiration-time=43200000",
		"spring.jwt.token.refresh-expiration-time=604800000",
		"payment.mock.base-url=https://payment.loopz.co.kr",
		"payment.mock.api-secret=test-payment-secret",
		"payment.mock.store-id=TEST-STORE-ID"
})
class CgvCloneApplicationTests {

	@Test
	void contextLoads() {
	}

}
