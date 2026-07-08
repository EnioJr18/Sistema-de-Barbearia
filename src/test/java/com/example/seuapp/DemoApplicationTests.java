package com.example.seuapp;

import com.seuapp.BarbeariaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@ActiveProfiles("test")
@SpringBootTest(
		classes = BarbeariaApplication.class,
		properties = {
				"spring.datasource.url=jdbc:h2:mem:barbearia_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
				"spring.datasource.username=sa",
				"spring.datasource.password=",
				"spring.datasource.driver-class-name=org.h2.Driver",
				"spring.jpa.hibernate.ddl-auto=create-drop",
				"spring.jpa.show-sql=false",
				"api.security.token.secret=segredo-ficticio-apenas-para-testes"
		})
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
