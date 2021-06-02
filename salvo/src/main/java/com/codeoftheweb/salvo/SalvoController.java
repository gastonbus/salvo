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
	private SalvoRepository salvoRepository;

	@Autowired
	private ScoreRepository scoreRepository;

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

		//Verify if this combination of game and player (gamePlayer) exists.
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "That game does not correspond to any player."), HttpStatus.BAD_REQUEST);
		}

		//Verify if this player is playing the game requested.
		GamePlayer gamePlayer = optGamePlayer.get();
		if(!(authenticatedPlayer.getId() == gamePlayer.getPlayer().getId())) {
			return new ResponseEntity<>(makeMap("error", "This is not your game. Don't try to cheat!"), HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(makeGameViewDTO(gamePlayer), HttpStatus.OK);

		/* Otra forma de hacerlo:

		if(!isGuest(authentication)) { //Verify if user is authenticated or not
			Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
			Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
			return optGamePlayer.map(gp -> {
				if(authenticatedPlayer.getId() == gp.getPlayer().getId()) {
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
		//Verify if user is authenticated or not
		if (isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		//Verify if this combination of game and player (gamePlayer) exists.
		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "The gamePlayer requested does not exist."), HttpStatus.FORBIDDEN);
		}

		//Verify if this player is playing the game requested.
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		GamePlayer gamePlayer = optGamePlayer.get();
		if(authenticatedPlayer.getId() != gamePlayer.getPlayer().getId()) {
			return new ResponseEntity<>(makeMap("error", "This game does not correspond to this player."), HttpStatus.FORBIDDEN);
		}

		//Validate if the player has his ships placed.
		if (gamePlayer.getShips().size() == 5) {
			return new ResponseEntity<>(makeMap("error", "You've just located your ships."), HttpStatus.FORBIDDEN);
		}

		//Validate if the request contains 5 ships.
		if (ships.size() != 5) {
			return new ResponseEntity<>(makeMap("error", "The request does not contains 5 ships."), HttpStatus.FORBIDDEN);
		}

		//Save the ships corresponding to the authenticated player for this game
		for (Ship ship:ships) {
			shipRepository.save(new Ship(gamePlayer, ship.getType(), ship.getLocations()));
		}
		return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
	}

	@GetMapping("/games/players/{gamePlayerId}/salvoes")
	public ResponseEntity<Map<String, Object>> getSalvoes(@PathVariable Long gamePlayerId, Authentication authentication) {

		//Verify if user is authenticated or not
		if(isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		//Verify if this combination of game and player (gamePlayer) exists.
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "That game does not correspond to any player."), HttpStatus.BAD_REQUEST);
		}

		//Verify if this player is playing the game requested.
		GamePlayer gamePlayer = optGamePlayer.get();
		if(!(authenticatedPlayer.getId() == gamePlayer.getPlayer().getId())) {
			return new ResponseEntity<>(makeMap("error", "This is not your game. Don't try to cheat!"), HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(makeGameViewDTO(gamePlayer), HttpStatus.OK);
	}


	@PostMapping("/games/players/{gamePlayerId}/salvoes")
	public ResponseEntity<Map<String, Object>> locateSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
		//Verify if user is authenticated or not
		if (isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("error", "You are not logged in!"), HttpStatus.UNAUTHORIZED);
		}

		//Verify if this combination of game and player (gamePlayer) exists.
		Optional<GamePlayer> optGamePlayer = gamePlayerRepository.findById(gamePlayerId);
		if (optGamePlayer.isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "The gamePlayer requested does not exist."), HttpStatus.FORBIDDEN);
		}

		//Verify if this player is playing the game requested.
		Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
		GamePlayer gamePlayer = optGamePlayer.get();
		if(authenticatedPlayer.getId() != gamePlayer.getPlayer().getId()) {
			return new ResponseEntity<>(makeMap("error", "This game does not correspond to this player."), HttpStatus.FORBIDDEN);
		}

		//Validate if the player has not fired all the salvoes.
		if (gamePlayer.getSalvoes().size() > 20) {
			return new ResponseEntity<>(makeMap("error", "You've just fired all your salvoes."), HttpStatus.FORBIDDEN);
		}

		//Validate if the request contains 5 shots.
		if (salvo.getLocations().size() != 5) {
			return new ResponseEntity<>(makeMap("error", "The request does not contains 5 shots."), HttpStatus.FORBIDDEN);
		}

		//Validate if the opponent has shot his salvo and it is the turn of the player.
		if (gamePlayer.getOpponent().isEmpty()) {
			return new ResponseEntity<>(makeMap("error", "There is no opponent yet."), HttpStatus.FORBIDDEN);
		}

		if (gamePlayer.getSalvoes().size() - gamePlayer.getOpponent().get().getSalvoes().size() >= 1) {
			return new ResponseEntity<>(makeMap("error", "You must wait for your opponent to make his shots."), HttpStatus.FORBIDDEN);
		}

		if (gamePlayer.gameStatus() == GameStatus.PLACE_SALVOES) {

			Salvo actualSalvo = new Salvo(gamePlayer, salvo.getTurn(), salvo.getLocations());
			salvoRepository.save(actualSalvo);

			gamePlayer.getSalvoes().add(actualSalvo);

			if (gamePlayer.gameStatus() == GameStatus.TIE) {
				scoreRepository.save(new Score(0.5, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getPlayer()));
				scoreRepository.save(new Score(0.5, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getOpponent().get().getPlayer()));
			} else if (gamePlayer.gameStatus() == GameStatus.WIN) {
				scoreRepository.save(new Score(1.0, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getPlayer()));
				scoreRepository.save(new Score(0.0, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getOpponent().get().getPlayer()));
			} else if (gamePlayer.gameStatus() == GameStatus.LOSE) {
				scoreRepository.save(new Score(0.0, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getPlayer()));
				scoreRepository.save(new Score(1.0, LocalDateTime.now(), gamePlayer.getGame(), gamePlayer.getOpponent().get().getPlayer()));
			}
			return new ResponseEntity<>(makeMap("turn", salvo.getTurn()), HttpStatus.CREATED);
		}

		//Save the salvoes corresponding to the authenticated player for this turn in this game
		salvoRepository.save(new Salvo(gamePlayer, salvo.getTurn(), salvo.getLocations()));

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
        dto.put("gameStatus", gamePlayer.gameStatus());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
        dto.put("ships", gamePlayer.getShips().stream().map(this::makeShipDTO).collect(toSet()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(elem -> elem.getSalvoes().stream().map(this::makeSalvoDTO)));
        dto.put("hits", gamePlayer.getSalvoes().stream().map(this::makeHitsDTO));
		dto.put("sunkShips", gamePlayer.getSalvoes().stream().map(this::makeSunkShipsDTO));
		dto.put("opponentHits", gamePlayer.getOpponent().map(gp -> gp.getSalvoes().stream().map(this::makeHitsDTO).collect(toList())).orElse(new ArrayList<>()));
		dto.put("opponentSunks", gamePlayer.getOpponent().map(gp -> gp.getSalvoes().stream().map(this::makeSunkShipsDTO).collect(toList())).orElse(new ArrayList<>()));
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

	//game_view/gamePlayer/hits
	private Map<String, Object> makeHitsDTO(Salvo salvo) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("turn", salvo.getTurn());
		dto.put("locationsWithImpact", salvo.getHits());
		return dto;
	}

	private Map<String, Object> makeSunkShipsDTO(Salvo salvo) {
		Map<String, Object> dto = new LinkedHashMap<>();
		dto.put("turn", salvo.getTurn());
		dto.put("ships", salvo.getSunkShips().stream().map(this::makeShipDTO));
		return dto;
	}

//	public String gameStatus(GamePlayer gamePlayer) {
//
//		if (this.isGameOver(gamePlayer)) {
//			return "game over";
//		} else {
//			if (gamePlayer.getOpponent().isEmpty()) {
//				return "waiting for opponent to join the game";
//			}
//			if (gamePlayer.getShips().isEmpty()) {
//				return "waiting for player to place ships";
//			}
//			if (gamePlayer.getOpponent().get().getShips().isEmpty()) {
//				return "waiting for opponent to place ships";
//			}
//			if (gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == gamePlayer.getSalvoes().size()).count() == 0) {
//				return "waiting for player salvo";
//			}
//			if (gamePlayer.getOpponent().get().getSalvoes().stream().filter(x -> x.getTurn() == gamePlayer.getSalvoes().size()).count() == 0) {
//				return "waiting for opponent salvo";
//			}
//			return "playing game";
//		}
//	}
//
//	Boolean isGameOver(GamePlayer gamePlayer) {
//		List<Long> shipsSunkByOpponent = gamePlayer.getSalvoes().stream().filter(x -> x.getTurn() == gamePlayer.getSalvoes().size()).flatMap(x -> x.getSunkShips().stream()).map(Ship::getId).collect(toList());
//		List<Long> shipsSunkByPlayer = new ArrayList<>();
//		if (gamePlayer.getOpponent().isPresent()) {
//			shipsSunkByPlayer = gamePlayer.getOpponent().get().getSalvoes().stream().filter(x -> x.getTurn() == gamePlayer.getOpponent().get().getSalvoes().size()).flatMap(x -> x.getSunkShips().stream()).map(Ship::getId).collect(toList());
//		}
//		return shipsSunkByPlayer.size() == 5 || shipsSunkByOpponent.size() == 5;
//	}

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
