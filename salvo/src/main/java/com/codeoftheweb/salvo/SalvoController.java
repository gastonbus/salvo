package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			return new ResponseEntity<>("error", HttpStatus.FORBIDDEN);
		}

		if (playerRepository.findByUserName(userName) !=  null) {
			return new ResponseEntity<>("error", HttpStatus.FORBIDDEN);
		}

		playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping("/games")
	public Map<String, Object> getGames(Authentication authentication) {
		Map<String, Object> dto = new LinkedHashMap<>();
		if (!isGuest(authentication)) {
			Player authenticatedPlayer = playerRepository.findByUserName(authentication.getName());
			dto.put("player", makePlayerDTO(authenticatedPlayer));
		} else {
			dto.put("player", null);
		}
		dto.put("games", gameRepository.findAll().stream().map(this::makeGameDTO).collect(toSet()));
		return dto;
	}

    @GetMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGame(@PathVariable Long gamePlayerId) {
        return makeGameViewDTO(gamePlayerRepository.findById(gamePlayerId).get());
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
        dto.put("id", gamePlayer.getId());
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

	private boolean isGuest(Authentication authentication) {
		return authentication == null || authentication instanceof AnonymousAuthenticationToken;
	}
}
