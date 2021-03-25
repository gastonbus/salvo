package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")

public class SalvoController {

    /*@Autowired
 http://localhost:8080/api/games   private PlayerRepository players;

    @RequestMapping("/players")
    public List<Player> getPlayers() {
        return players.findAll();
    }*/

    @Autowired
    private GameRepository games;

    @RequestMapping("/games")
    public List<Game> getGames() {
        return games.findAll();
    }
}

