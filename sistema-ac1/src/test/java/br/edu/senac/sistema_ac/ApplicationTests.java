package br.edu.senac.sistema_ac;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"DB_PASSWORD=root",
	"EMAIL_USERNAME=test@example.com",
	"EMAIL_PASSWORD=test-password"
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
