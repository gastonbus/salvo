package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/players")
    public List<Player> getPlayers(){
        return playerRepository.findAll();
    }

    @RequestMapping("/games")
    public Set<Long> getGames() {
        return gameRepository.findAll().stream().map(Game::getId).collect(toSet());
    }
}

