var app = new Vue({
    el: '#app',
    data: {
        game: [],
        rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
        columns: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
        cellsWithShips: [],
        players: {
            playerA: {
                email: "",
            },
            playerB: {
                email: "",
            }
        },
    },
    methods: {
        savePlayers: function () {
            app.players.playerA = app.game.gamePlayers.find(element => element.id == gamePlayerId).player;
            app.players.playerB = app.game.gamePlayers.find(element => element.id != gamePlayerId).player;
        },
        locateShips: function () {
            for (let i = 0; i < app.game.ships.length; i++) {
                switch (app.game.ships[i].type) {
                    case "Battleship":
                        app.cellsWithShips.battleship = app.game.ships[i].locations;
                        //Paint battleship cells:
                        for (let i = 0; i < app.cellsWithShips.battleship.length; i++) {
                            document.getElementById(app.cellsWithShips.battleship[i]).className = "battleship";
                        }
                        break;
                    case "Submarine":
                        app.cellsWithShips.submarine = app.game.ships[i].locations;
                        //Paint submarine cells:
                        for (let i = 0; i < app.cellsWithShips.submarine.length; i++) {
                            document.getElementById(app.cellsWithShips.submarine[i]).className = "submarine";
                        }
                        break;
                    case "Destroyer":
                        app.cellsWithShips.destroyer = app.game.ships[i].locations;
                        //Paint destroyer cells:
                        for (let i = 0; i < app.cellsWithShips.destroyer.length; i++) {
                            document.getElementById(app.cellsWithShips.destroyer[i]).className = "destroyer";
                        }
                        break;
                    case "Carrier":
                        app.cellsWithShips.carrier = app.game.ships[i].locations;
                        //Paint carrier cells:
                        for (let i = 0; i < app.cellsWithShips.carrier.length; i++) {
                            document.getElementById(app.cellsWithShips.carrier[i]).className = "carrier";
                        }
                        break;
                    case "Patrol Boat":
                        app.cellsWithShips.patrolBoat = app.game.ships[i].locations;
                        //Paint patrolBoat cells:
                        for (let i = 0; i < app.cellsWithShips.patrolBoat.length; i++) {
                            document.getElementById(app.cellsWithShips.patrolBoat[i]).className = "patrolBoat";
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
});

const urlParams = new URLSearchParams(window.location.search);
const gamePlayerId = urlParams.get('gp');

fetch('http://localhost:8080/api/game_view/' + gamePlayerId)
    .then(response => response.json())
    .then(data => {
        app.game = data;
        app.savePlayers();
        app.locateShips();
    })