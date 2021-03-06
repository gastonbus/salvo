package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	//@Bean
	public CommandLineRunner initData(
			PlayerRepository playerRepository,
			GameRepository gameRepository,
			GamePlayerRepository gamePlayerRepository,
			ShipRepository shipRepository,
			SalvoRepository salvoRepository,
			ScoreRepository scoreRepository
	) {
		return (args) -> {

			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder.encode("24"));
			Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder.encode("42"));
			Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder.encode("kb"));
			Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder.encode("mole"));

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
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);
			gameRepository.save(game7);

			GamePlayer gamePlayer1 = new GamePlayer(LocalDateTime.now(), player1, game1);
			GamePlayer gamePlayer2 = new GamePlayer(LocalDateTime.now(), player2, game1);
			GamePlayer gamePlayer3 = new GamePlayer(LocalDateTime.now(), player3, game2);
			GamePlayer gamePlayer4 = new GamePlayer(LocalDateTime.now(), player4, game2);
			GamePlayer gamePlayer5 = new GamePlayer(LocalDateTime.now(), player1, game3);
			GamePlayer gamePlayer6 = new GamePlayer(LocalDateTime.now(), player4, game3);
			GamePlayer gamePlayer7 = new GamePlayer(LocalDateTime.now(), player2, game4);
			GamePlayer gamePlayer8 = new GamePlayer(LocalDateTime.now(), player3, game4);
			GamePlayer gamePlayer9 = new GamePlayer(LocalDateTime.now(), player1, game5);
			GamePlayer gamePlayer10 = new GamePlayer(LocalDateTime.now(), player3, game5);
			GamePlayer gamePlayer11 = new GamePlayer(LocalDateTime.now(), player2, game6);
			GamePlayer gamePlayer12 = new GamePlayer(LocalDateTime.now(), player4, game6);
			GamePlayer gamePlayer13 = new GamePlayer(LocalDateTime.now(), player4, game7);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);
			gamePlayerRepository.save(gamePlayer10);
			gamePlayerRepository.save(gamePlayer11);
			gamePlayerRepository.save(gamePlayer12);
			gamePlayerRepository.save(gamePlayer13);

			shipRepository.save(new Ship(gamePlayer1, "Carrier", Arrays.asList("A1", "A2", "A3", "A4", "A5")));
			shipRepository.save(new Ship(gamePlayer1, "Battleship", Arrays.asList("B1", "B2", "B3", "B4")));
			shipRepository.save(new Ship(gamePlayer1, "Submarine", Arrays.asList("C1", "C2", "C3")));
			shipRepository.save(new Ship(gamePlayer1, "Destroyer", Arrays.asList("D1", "D2", "D3")));
			shipRepository.save(new Ship(gamePlayer1, "Patrol Boat", Arrays.asList("E1", "E2")));

			shipRepository.save(new Ship(gamePlayer2, "Carrier", Arrays.asList("A6", "A7", "A8", "A9", "A10")));
			shipRepository.save(new Ship(gamePlayer2, "Battleship", Arrays.asList("B7", "B8", "B9", "B10")));
			shipRepository.save(new Ship(gamePlayer2, "Submarine", Arrays.asList("C8", "C9", "C10")));
			shipRepository.save(new Ship(gamePlayer2, "Destroyer", Arrays.asList("D8", "D9", "D10")));
			shipRepository.save(new Ship(gamePlayer2, "Patrol Boat", Arrays.asList("E9", "E10")));

			shipRepository.save(new Ship(gamePlayer3, "Carrier", Arrays.asList("F3", "G3", "H3", "I3", "J3")));
			shipRepository.save(new Ship(gamePlayer3, "Battleship", Arrays.asList("D6", "D7", "D8", "D9")));
			shipRepository.save(new Ship(gamePlayer3, "Submarine", Arrays.asList("C2", "D2", "E2")));
			shipRepository.save(new Ship(gamePlayer3, "Destroyer", Arrays.asList("J5", "J6", "J7")));
			shipRepository.save(new Ship(gamePlayer3, "Patrol Boat", Arrays.asList("F8", "G8")));

			shipRepository.save(new Ship(gamePlayer4, "Carrier", Arrays.asList("F6", "F7", "F8", "F9", "F10")));
			shipRepository.save(new Ship(gamePlayer4, "Battleship", Arrays.asList("E2", "F2", "G2", "H2")));
			shipRepository.save(new Ship(gamePlayer4, "Submarine", Arrays.asList("C3", "C4", "C5")));
			shipRepository.save(new Ship(gamePlayer4, "Destroyer", Arrays.asList("B8", "C8", "D8")));
			shipRepository.save(new Ship(gamePlayer4, "Patrol Boat", Arrays.asList("I5", "I6")));

			shipRepository.save(new Ship(gamePlayer5, "Carrier", Arrays.asList("F1", "G1", "H1", "I1", "J1")));
			shipRepository.save(new Ship(gamePlayer5, "Battleship", Arrays.asList("A4", "A5", "A6", "A7")));
			shipRepository.save(new Ship(gamePlayer5, "Submarine", Arrays.asList("A1", "B1", "C1")));
			shipRepository.save(new Ship(gamePlayer5, "Destroyer", Arrays.asList("H2", "H3", "H4")));
			shipRepository.save(new Ship(gamePlayer5, "Patrol Boat", Arrays.asList("C10", "D10")));

			shipRepository.save(new Ship(gamePlayer6, "Carrier", Arrays.asList("J1", "J2", "J3", "J4", "J5")));
			shipRepository.save(new Ship(gamePlayer6, "Battleship", Arrays.asList("C3", "D3", "E3", "F3")));
			shipRepository.save(new Ship(gamePlayer6, "Submarine", Arrays.asList("A1", "A2", "A3")));
			shipRepository.save(new Ship(gamePlayer6, "Destroyer", Arrays.asList("E9", "F9", "G9")));
			shipRepository.save(new Ship(gamePlayer6, "Patrol Boat", Arrays.asList("F5", "F6")));

			shipRepository.save(new Ship(gamePlayer7, "Carrier", Arrays.asList("E9", "F9", "G9", "H9", "I9")));
			shipRepository.save(new Ship(gamePlayer7, "Battleship", Arrays.asList("E2", "E3", "E4", "E5")));
			shipRepository.save(new Ship(gamePlayer7, "Submarine", Arrays.asList("G4", "H4", "I4")));
			shipRepository.save(new Ship(gamePlayer7, "Destroyer", Arrays.asList("J1", "J2", "J3")));
			shipRepository.save(new Ship(gamePlayer7, "Patrol Boat", Arrays.asList("F8", "G8")));

			shipRepository.save(new Ship(gamePlayer8, "Carrier", Arrays.asList("G3", "G4", "G5", "G6", "G7")));
			shipRepository.save(new Ship(gamePlayer8, "Battleship", Arrays.asList("E1", "F1", "G1", "H1")));
			shipRepository.save(new Ship(gamePlayer8, "Submarine", Arrays.asList("E8", "E9", "E10")));
			shipRepository.save(new Ship(gamePlayer8, "Destroyer", Arrays.asList("A6", "B6", "C6")));
			shipRepository.save(new Ship(gamePlayer8, "Patrol Boat", Arrays.asList("I2", "I3")));

			shipRepository.save(new Ship(gamePlayer9, "Carrier", Arrays.asList("F3", "G3", "H3", "I3", "J3")));
			shipRepository.save(new Ship(gamePlayer9, "Battleship", Arrays.asList("A7", "A8", "A9", "A10")));
			shipRepository.save(new Ship(gamePlayer9, "Submarine", Arrays.asList("C5", "D5", "E5")));
			shipRepository.save(new Ship(gamePlayer9, "Destroyer", Arrays.asList("I5", "I6", "I7")));
			shipRepository.save(new Ship(gamePlayer9, "Patrol Boat", Arrays.asList("A1", "B1")));

			shipRepository.save(new Ship(gamePlayer10, "Carrier", Arrays.asList("A1", "A2", "A3", "A4", "A5")));
			shipRepository.save(new Ship(gamePlayer10, "Battleship", Arrays.asList("F9", "G9", "H9", "I9")));
			shipRepository.save(new Ship(gamePlayer10, "Submarine", Arrays.asList("D4", "D5", "D6")));
			shipRepository.save(new Ship(gamePlayer10, "Destroyer", Arrays.asList("D10", "E10", "F10")));
			shipRepository.save(new Ship(gamePlayer10, "Patrol Boat", Arrays.asList("I1", "I2")));

			shipRepository.save(new Ship(gamePlayer11, "Carrier", Arrays.asList("B2", "C2", "D2", "E2", "F2")));
			shipRepository.save(new Ship(gamePlayer11, "Battleship", Arrays.asList("B7", "B8", "B9", "B10")));
			shipRepository.save(new Ship(gamePlayer11, "Submarine", Arrays.asList("D4", "E4", "F4")));
			shipRepository.save(new Ship(gamePlayer11, "Destroyer", Arrays.asList("J3", "J4", "J5")));
			shipRepository.save(new Ship(gamePlayer11, "Patrol Boat", Arrays.asList("G4", "H4")));

			shipRepository.save(new Ship(gamePlayer12, "Carrier", Arrays.asList("G4", "G5", "G6", "G7", "G8")));
			shipRepository.save(new Ship(gamePlayer12, "Battleship", Arrays.asList("E3", "F3", "G3", "H3")));
			shipRepository.save(new Ship(gamePlayer12, "Submarine", Arrays.asList("I1", "I2", "I3")));
			shipRepository.save(new Ship(gamePlayer12, "Destroyer", Arrays.asList("B6", "C6", "D6")));
			shipRepository.save(new Ship(gamePlayer12, "Patrol Boat", Arrays.asList("H7", "H8")));

			shipRepository.save(new Ship(gamePlayer13, "Carrier", Arrays.asList("E10", "F10", "G10", "H10", "I10")));
			shipRepository.save(new Ship(gamePlayer13, "Battleship", Arrays.asList("D7", "D8", "D9", "D10")));
			shipRepository.save(new Ship(gamePlayer13, "Submarine", Arrays.asList("A9", "B9", "C9")));
			shipRepository.save(new Ship(gamePlayer13, "Destroyer", Arrays.asList("I7", "I8", "I9")));
			shipRepository.save(new Ship(gamePlayer13, "Patrol Boat", Arrays.asList("F1", "G1")));

			salvoRepository.save(new Salvo(gamePlayer1, 1, Arrays.asList("A8", "B8", "C8", "D8", "B10")));
			salvoRepository.save(new Salvo(gamePlayer2, 1, Arrays.asList("C1", "C2", "C3", "D4", "E4")));
			salvoRepository.save(new Salvo(gamePlayer1, 2, Arrays.asList("B7", "C9", "C2", "B9", "J5")));
			salvoRepository.save(new Salvo(gamePlayer2, 2, Arrays.asList("E1", "E2", "E3", "H6", "F7")));
			salvoRepository.save(new Salvo(gamePlayer1, 3, Arrays.asList("B9", "D1", "D3", "D5", "H8")));
			salvoRepository.save(new Salvo(gamePlayer2, 3, Arrays.asList("B6", "B10", "D2", "G6", "H9")));

			scoreRepository.save(new Score(0.5, LocalDateTime.now(), game1, player1));
			scoreRepository.save(new Score(0.5, LocalDateTime.now(), game1, player2));
			scoreRepository.save(new Score(0.0, LocalDateTime.now(), game2, player3));
			scoreRepository.save(new Score(1.0, LocalDateTime.now(), game2, player4));
			scoreRepository.save(new Score(0.5, LocalDateTime.now(), game3, player1));
			scoreRepository.save(new Score(0.5, LocalDateTime.now(), game3, player4));
			scoreRepository.save(new Score(1.0, LocalDateTime.now(), game4, player2));
			scoreRepository.save(new Score(0.0, LocalDateTime.now(), game4, player4));
			scoreRepository.save(new Score(1.0, LocalDateTime.now(), game5, player1));
			scoreRepository.save(new Score(0.0, LocalDateTime.now(), game5, player3));
			scoreRepository.save(new Score(1.0, LocalDateTime.now(), game6, player2));
			scoreRepository.save(new Score(0.0, LocalDateTime.now(), game6, player4));
		};
	}
}

//Authentication
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName -> {
			Player player = playerRepository.findByUserName(inputName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

//Authorization
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/web/game.html", "/api/game_view/**", "/rest/**", "/h2-console/**").hasAuthority("USER")
				.antMatchers("/**").permitAll();

		http.formLogin()
				.usernameParameter("userName")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		http.headers().frameOptions().disable();

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}