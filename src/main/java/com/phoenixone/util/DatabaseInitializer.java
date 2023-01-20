package com.phoenixone.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseInitializer {

	@Autowired JdbcTemplate jdbcTemplate;
	
	@Bean
	CommandLineRunner initDatabase() {
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {
				jdbcTemplate.execute("CREATE TABLE ACCOUNTS (id int primary key auto_increment, "
						+ "customerNumber varchar(8), customerName varchar(50), customerMobile varchar(20), "
						+ "customerEmail varchar(50), address1 varchar(100), address2 varchar(100), "
						+ "accountType varchar(1), accountNumber varchar(5))");
				jdbcTemplate.execute("CREATE TABLE TRANSACTIONS (id int primary key auto_increment, "
						+ "account_number varchar(5), description varchar(100), amount decimal(10,2))");
			}
		};
	}
}
