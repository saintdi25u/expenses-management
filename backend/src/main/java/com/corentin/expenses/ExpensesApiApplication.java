package com.corentin.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExpensesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpensesApiApplication.class, args);
    }
}
