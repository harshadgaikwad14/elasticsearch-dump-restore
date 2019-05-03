package com.example.demo;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootApplication
public class ElasticsearchDumpRestoreApplication {

	public static void main(String[] args)
			throws URISyntaxException, JsonProcessingException, IOException, InterruptedException {
		SpringApplication.run(ElasticsearchDumpRestoreApplication.class, args);

	}

}
