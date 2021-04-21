package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
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

	@RequestMapping(path = "/players", method = RequestMethod.POST)
	public List<Player> getPlayers() {
		return playerRepository.findAll();
	}

/*
	@Autowired
	private PasswordEncoder passwordEncoder;

	public ResponseEntity<Object> register(
		@RequestParam String userName,
		@RequestParam String password
	) {

		if (userName.isEmpty() || password.isEmpty()) {
			return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
		}

		if (playerRepository.findByUserName(userName) !=  null) {
			return new ResponseEntity<>("Username already in use", HttpStatus.FORBIDDEN);
		}

		playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
*/

    @RequestMapping("/games")
    public Set<Map<String, Object>> getGames() {
        return gameRepository.findAll().stream().map(this::makeGameDTO).collect(toSet());
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String, Object> getGame(@PathVariable Long gamePlayerId) {
        return makeGameViewDTO(gamePlayerRepository.findById(gamePlayerId).get());
    }

    public Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("date", game.getDateTime());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
        return dto;
    }

    //game_view
    public Map<String, Object> makeGameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("date", gamePlayer.getGame().getDateTime());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(this::makeGamePlayerDTO).collect(toSet()));
        dto.put("ships", gamePlayer.getShips().stream().map(this::makeShipDTO).collect(toSet()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(elem -> elem.getSalvoes().stream().map(this::makeSalvoDTO)));
        return dto;
    }

    //game_view/gamePlayer
    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("score", gamePlayer.getScore().map(Score::getScore).orElse(null));
        return dto;
    }

    //game_view/gamePlayer/player
    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    //game_view/gamePlayer/ships
    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //game_view/gamePlayer/salvoes
    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerId", salvo.getGamePlayer().getPlayer().getId());
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getLocations());
        return dto;
    }
}
