package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    //Properties
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch=FetchType.EAGER)
    private Set<Salvo> salvoes = new HashSet<>();

    //Constructors
    public GamePlayer() { }

    public GamePlayer(LocalDateTime dateTime, Player player, Game game) {
        this.dateTime = dateTime;
        this.player = player;
        this.game = game;
    }

    public Optional<Score> getScore() {
        return player.getScore(game);
    }

    //Setters and Getters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }

	public Optional<GamePlayer> getOpponent() {
		return this.getGame().getGamePlayers().stream().filter(elem -> elem.getId() != this.getId()).findFirst();
	}

	public GameStatus gameStatus() {
		if (this.getShips().isEmpty()) {
			return GameStatus.PLACE_SHIPS;
		} else {
			if (this.getOpponent().isPresent()) {
				if (this.getOpponent().get().getShips().isEmpty()) {
					return GameStatus.WAIT_OPPONENT;
				} else {
					if (this.getSalvoes().stream().noneMatch(em -> em.getTurn() == this.getSalvoes().size())) {
						return GameStatus.PLACE_SALVOES;
					} else {
						if (this.getOpponent().get().getSalvoes().stream().noneMatch(em -> em.getTurn() == this.getSalvoes().size())) {
							return GameStatus.WAIT_OPPONENT;
						} else if(this.getSalvoes().size() == this.getOpponent().get().getSalvoes().size()){
							List<Long> shipsSunkByPlayer = this.getSalvoes().stream().filter(x -> x.getTurn() == this.getSalvoes().size()).flatMap(x -> x.getSunkShips().stream()).map(Ship::getId).collect(toList());
							List<Long> shipsSunkByOpponent = new ArrayList<>();

							if (this.getOpponent().isPresent()) {
								shipsSunkByOpponent = this.getOpponent().get().getSalvoes().stream().filter(x -> x.getTurn() == this.getSalvoes().size()).flatMap(x -> x.getSunkShips().stream()).map(Ship::getId).collect(toList());
							}

							if (shipsSunkByPlayer.size() == 5 && shipsSunkByOpponent.size() == 5) {
								return GameStatus.TIE;
							} else if ( shipsSunkByPlayer.size() == 5) {
								return GameStatus.WIN;
							} else if (shipsSunkByOpponent.size() == 5) {
								return GameStatus.LOSE;
							}else{
								return GameStatus.PLACE_SALVOES;
							}
						}
						else {
							return GameStatus.PLACE_SALVOES;
						}
					}
				}
			}else{
				return GameStatus.WAIT_OPPONENT;
			}
		}

	}

}
