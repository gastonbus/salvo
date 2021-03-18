package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository) {
		return (args) -> {
			playerRepository.save(new Player("j.bauer@ctu.gov"));
			playerRepository.save(new Player("c.obrian@ctu.gov"));
			playerRepository.save(new Player("kim_bauer@gmail.com"));
			playerRepository.save(new Player("t.almeida@ctu.gov"));

			gameRepository.save(new Game("15/05/2021"));
			gameRepository.save(new Game("18/06/2021"));
			gameRepository.save(new Game("19/07/2021"));
		};
	}
}
