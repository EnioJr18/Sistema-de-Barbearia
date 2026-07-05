package com.example.demo;

import com.seuapp.BarbeariaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@ActiveProfiles("test")
@SpringBootTest(classes = BarbeariaApplication.class)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
