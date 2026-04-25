package com.tylerlam.expensetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration," +
    "org.springframework.boot.orm.jpa.hibernate.SpringJtaPlatform," +
    "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class ExpenseTrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
