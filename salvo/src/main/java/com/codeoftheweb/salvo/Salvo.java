package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Entity
public class Salvo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "salvo_id")
	private GamePlayer gamePlayer;

	private int turn;

	@ElementCollection
	@Column(name = "locations")
	private List<String> locations = new ArrayList<>();


	public Salvo() {
	}

	public Salvo(GamePlayer gamePlayer, int turn, List<String> locations) {
		this.gamePlayer = gamePlayer;
		this.turn = turn;
		this.locations = locations;
	}

	public Long getId() { return id; }

	public void setId(Long id) {
		this.id = id;
	}

	public GamePlayer getGamePlayer() {
		return gamePlayer;
	}

	public void setGamePlayer(GamePlayer gamePlayer) {
		this.gamePlayer = gamePlayer;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public List<String> getLocations() {
		return locations;
	}

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public List<String> getHits() {
		return gamePlayer.getOpponent().map(gp -> {
			List<String> ships = gp.getShips().stream().flatMap(ship -> ship.getLocations().stream()).collect(toList());
			return ships.stream().filter(ship -> this.locations.contains(ship)).collect(toList());
		}).orElse(new ArrayList<>());
	}

	public List<Ship> getSunkShips() {
		return gamePlayer.getOpponent().map(gp -> {
			List<String> allHitsLocations = this.gamePlayer.getSalvoes().stream().filter(salvo -> salvo.getTurn() <= this.turn).flatMap(salvo -> salvo.getHits().stream()).collect(toList());
			return gp.getShips().stream().filter(ship -> allHitsLocations.containsAll(ship.getLocations())).collect(toList());
		}).orElse(new ArrayList<>());
	}
}