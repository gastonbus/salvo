package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {


	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(
			PlayerRepository playerRepository,
			GameRepository gameRepository,
			GamePlayerRepository gamePlayerRepository,
			ShipRepository shipRepository
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
			Game game4 = new Game(LocalDateTime.now().plusHours(3));
			Game game5 = new Game(LocalDateTime.now().plusHours(4));
			Game game6 = new Game(LocalDateTime.now().plusHours(5));
			Game game7 = new Game(LocalDateTime.now().plusHours(6));
			Game game8 = new Game(LocalDateTime.now().plusHours(7));

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);

			GamePlayer gamePlayer1 = new GamePlayer(LocalDateTime.now(), player1, game1);
			GamePlayer gamePlayer2 = new GamePlayer(LocalDateTime.now(), player2, game1);
			GamePlayer gamePlayer3 = new GamePlayer(LocalDateTime.now(), player3, game2);
			GamePlayer gamePlayer4 = new GamePlayer(LocalDateTime.now(), player4, game2);
			GamePlayer gamePlayer5 = new GamePlayer(LocalDateTime.now(), player4, game3);


			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);

			shipRepository.save(new Ship(gamePlayer1, "Carrier", Arrays.asList("C4", "D4", "E4", "F4", "G4")));
			shipRepository.save(new Ship(gamePlayer1, "Battleship", Arrays.asList("E6", "E7", "E8", "E9")));
			shipRepository.save(new Ship(gamePlayer1, "Submarine", Arrays.asList("G1", "H1", "I1")));
			shipRepository.save(new Ship(gamePlayer1, "Destroyer", Arrays.asList("J5", "J6", "J7")));
			shipRepository.save(new Ship(gamePlayer1, "Patrol Boat", Arrays.asList("H10", "I10")));

			shipRepository.save(new Ship(gamePlayer2, "Carrier", Arrays.asList("H5", "H6", "H7", "H8", "H9")));
			shipRepository.save(new Ship(gamePlayer2, "Battleship", Arrays.asList("C1", "D1", "E1", "F1")));
			shipRepository.save(new Ship(gamePlayer2, "Submarine", Arrays.asList("A5", "A6", "A7")));
			shipRepository.save(new Ship(gamePlayer2, "Destroyer", Arrays.asList("A10", "B10", "C10")));
			shipRepository.save(new Ship(gamePlayer2, "Patrol Boat", Arrays.asList("J2", "J3")));

		};
	}
}
