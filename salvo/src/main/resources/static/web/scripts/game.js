var app = new Vue({
    el: '#app',
    data: {
        game: [],
        rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
        columns: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
        cellsWithShips: [],
    },
});

const urlParams = new URLSearchParams(window.location.search);
const gamePlayerId = urlParams.get('gp');

fetch('http://localhost:8080/api/game_view/' + gamePlayerId)
    .then(response => response.json())
    .then(data => {
        app.game = data;
        for (let i = 0; i < app.game[0].ships.length; i++) {
            switch (app.game[0].ships[i].type) {
                case "Battleship":
                    app.cellsWithShips.battleship = app.game[0].ships[i].locations;
                    break;
                case "Submarine":
                    app.cellsWithShips.submarine = app.game[0].ships[i].locations;
                    break;
                case "Destroyer":
                    app.cellsWithShips.destroyer = app.game[0].ships[i].locations;
                    break;
                case "Carrier":
                    app.cellsWithShips.carrier = app.game[0].ships[i].locations;
                    break;
                case "Patrol Boat":
                    app.cellsWithShips.patrolBoat = app.game[0].ships[i].locations;
                    break;
                default:
                    break;
            }
        }

        //Paint battleship cells
        for (let i = 0; i < app.cellsWithShips.battleship.length; i++) {
            document.getElementById(app.cellsWithShips.battleship[i]).style.backgroundColor = "red";
        }

        //Paint submarine cells
        for (let i = 0; i < app.cellsWithShips.submarine.length; i++) {
            document.getElementById(app.cellsWithShips.submarine[i]).style.backgroundColor = "blue";
        }

        //Paint destroyer cells
        for (let i = 0; i < app.cellsWithShips.destroyer.length; i++) {
            document.getElementById(app.cellsWithShips.destroyer[i]).style.backgroundColor = "green";
        }

        //Paint carrier cells
        for (let i = 0; i < app.cellsWithShips.carrier.length; i++) {
            document.getElementById(app.cellsWithShips.carrier[i]).style.backgroundColor = "orange";
        }

        //Paint patrolBoat cells
        for (let i = 0; i < app.cellsWithShips.patrolBoat.length; i++) {
            document.getElementById(app.cellsWithShips.patrolBoat[i]).style.backgroundColor = "yellow";
        }
        
        document.getElementById("main-title").innerText = "Battle between " + app.game[0].gamePlayers.find(element => element.id == gamePlayerId).player.email + " (you) and " + app.game[0].gamePlayers.find(element => element.id != gamePlayerId).player.email + "."; 
    })