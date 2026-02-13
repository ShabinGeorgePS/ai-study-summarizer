package com.shabin.aistudysummarizer;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AistudysummarizerApplication {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(AistudysummarizerApplication.class, args);
	}

	private static void loadEnvFile() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory(System.getProperty("user.dir"))
					.ignoreIfMissing()
					.load();
			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""));
		} catch (DotenvException e) {
			// .env not found or empty - use system env or defaults from application.yml
		}
	}

}
