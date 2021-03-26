package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@SpringBootApplication
public class SalvoApplication {


	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			PlayerRepository playerRepository,
			GameRepository gameRepository,
			GamePlayerRepository gamePlayerRepository
	) {
		return (args) -> {

			Player player1 = new Player("j.bauer@ctu.gov");
			Player player2 = new Player("c.obrian@ctu.gov");
			Player player3 = new Player("kim_bauer@gmail.com");
			Player player4 = new Player("t.almeida@ctu.gov");

			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			Game game1 = new Game(LocalDateTime.now());
			Game game2 = new Game(LocalDateTime.now().plusHours(1));
			Game game3 = new Game(LocalDateTime.now().plusHours(2));

			gameRepository.save(game1);
			gameRepository.save(game2);

			gamePlayerRepository.save(new GamePlayer(LocalDateTime.now(), player1, game1));
			gamePlayerRepository.save(new GamePlayer(LocalDateTime.now(), player2, game2));
			gamePlayerRepository.save(new GamePlayer(LocalDateTime.now(), player3, game1));
			gamePlayerRepository.save(new GamePlayer(LocalDateTime.now(), player4, game2));
		};
	}
}
