package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/players")
	public Set<Map<String, Object>> getPlayers() {
		return playerRepository.findAll().stream().map(this::makePlayerDTO).collect(toSet());
	}

	@PostMapping("/players")
	public ResponseEntity<Object> register(
		@RequestParam String userName,
		@RequestParam String password
	) {

		if (userName.isEmpty() || password.isEmpty()) {
			return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
		}

		if (playerRepository.findByUserName(userName) !=  null) {
			return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
		}

		playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping("/games")
	public Map<String, Object> getGames(Authentication authentication) {
		Map<String, Object> dto = new LinkedHashMap<>();

		//Verify if user is authenticated or not
		if (!isGuest(authentication)) {
			Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
			dto.put("player", makePlayerDTO(authenticatedPlayer));
		} else {
			dto.put("player", null);
		}
		dto.put("games", gameRepository.findAll().stream().map(this::makeGameDTO).collect(toSet()));
		return dto;
	}

	//Function to create new game
	@PostMapping("/games")
	public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
		if (!isGuest(authentication)) {
			LocalDateTime dateTime = LocalDateTime.now();
			Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());

			//Procedure to create a new game:
			Game newGame = new Game(dateTime);
			gameRepository.save(newGame);

			GamePlayer newGamePlayer = new GamePlayer(dateTime, authenticatedPlayer, newGame);
			gamePlayerRepository.save(newGamePlayer);

			return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

		} else {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}
	}

	//Function to join an existing game
	@PostMapping("/games/{gameId}/players")
	public ResponseEntity<Map<String,Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {

		//Verify if user is authenticated or not
		if (isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		LocalDateTime dateTime = LocalDateTime.now();
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		Optional<Game> optRequestedGame = gameRepository.findById(gameId);

		//Verify that the game exists
		if (optRequestedGame.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "The game you want to play does not exist."), HttpStatus.FORBIDDEN);
		}

		//Verify if the player is full or it have space for one more player
		Game requestedGame = optRequestedGame.get();
		if (requestedGame.getGamePlayers().size() >= 2) {
			return new ResponseEntity<>(makeMap("error", "The game is full and you can't join it."), HttpStatus.FORBIDDEN);
		}

		//Verify if the existing player is not the authenticated player
		Optional<Player> existingPlayer = requestedGame.getPlayers().stream().findFirst();
		if (!(existingPlayer.isPresent() && authenticatedPlayer.getId() != existingPlayer.get().getId())) {
			return new ResponseEntity<>(makeMap("error", "You are already in this game."), HttpStatus.FORBIDDEN);
		}

		//Procedure to join a game:
		GamePlayer newGamePlayer = new GamePlayer(dateTime, authenticatedPlayer, requestedGame);
		gamePlayerRepository.save(newGamePlayer);
		return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
	}

    @GetMapping("/game_view/{gamePlayerId}")
	public ResponseEntity<Map<String, Object>> getGame(@PathVariable Long gamePlayerId, Authentication authentication) {

		//Verify if user is authenticated or not
		if(isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);

		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "That game does not correspond to any player."), HttpStatus.BAD_REQUEST);
		}

		GamePlayer gamePlayer = optGamePlayer.get();

		//Verify if this player is playing the game requested.
		if(!(authenticatedPlayer.getId() == gamePlayer.getPlayer().getId())) {
			return new ResponseEntity<>(makeMap("error", "This is not your game. Don't try to cheat!"), HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(makeGameViewDTO(gamePlayer), HttpStatus.OK);

		/* Otra forma de hacerlo:

		if(!isGuest(authentication)) { //Verify if user is authenticated or not
			Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
			Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
			return optGamePlayer.map(gp -> {
				if(authenticatedPlayer.getId() == gp.getPlayer().getId()) { //Verify if this player is playing the game requested.
					return new ResponseEntity<>(makeGameViewDTO(gp), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(makeMap("error", "This is not your game. Don't try to cheat!"), HttpStatus.FORBIDDEN);
				}
			})
				.orElse(new ResponseEntity<>(makeMap("error", "That game does not correspond to any player."), HttpStatus.BAD_REQUEST));
		} else {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}*/
	}

	@PostMapping("/games/players/{gamePlayerId}/ships")
	public ResponseEntity<Map<String, Object>> locateShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships, Authentication authentication) {
		if (isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "The gamePlayer requested does not exist."), HttpStatus.UNAUTHORIZED);
		}

		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		GamePlayer gamePlayer = optGamePlayer.get();

		//Verify if this player is playing the game requested.
		if(authenticatedPlayer.getId() != gamePlayer.getPlayer().getId()) {
			return new ResponseEntity<>(makeMap("error", "This game does not correspond to this player."), HttpStatus.UNAUTHORIZED);
		}

		//Validate if the player has his ships placed.
		if (shipRepository.findAll().stream().filter(ship -> ship.getGamePlayer().equals(gamePlayer)).count() == 5) {
			return new ResponseEntity<>(makeMap("error", "You've just located your ships."), HttpStatus.FORBIDDEN);
		}

		//Validate if the request contains 5 ships.
		if (ships.size() != 5) {
			return new ResponseEntity<>(makeMap("error", "The request does not contains 5 ships."), HttpStatus.FORBIDDEN);
		}

		//Save the ships corresponding to the authenticated player
		for (Ship ship:ships) {
			shipRepository.save(new Ship(gamePlayer, ship.getType(), ship.getLocations()));
		}
		return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
	}

	public Map<String, Object> makeGamesDTO(Game game) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("id", game.getId());
		dto.put("date", game.getDateTime());
		dto.put("gamePlayers", game.getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
		return dto;
	}

	public Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("date", game.getDateTime());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
        return dto;
    }

    //game_view
    public Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("date", gamePlayer.getGame().getDateTime());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
        dto.put("ships", gamePlayer.getShips().stream().map(this::makeShipDTO).collect(toSet()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(elem -> elem.getSalvoes().stream().map(this::makeSalvoDTO)));
        return dto;
    }

    //game_view/gamePlayer
    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("score", gamePlayer.getScore().map(Score::getScore).orElse(null));
        return dto;
    }

    //game_view/gamePlayer/player
    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    //game_view/gamePlayer/ships
    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //game_view/gamePlayer/salvoes
    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("playerId", salvo.getGamePlayer().getPlayer().getId());
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    //Function to help find if the user is authenticated or not
	private boolean isGuest(Authentication authentication) {
		return authentication == null || authentication instanceof AnonymousAuthenticationToken;
	}

	//Function to create a Json used in responses
	private Map<String, Object> makeMap(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		return map;
	}
}
