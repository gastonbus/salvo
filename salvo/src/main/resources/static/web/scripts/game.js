var app = new Vue({
    el: '#app',
    data: {
        game: [],
        rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
        columns: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
        cellsWithShips: [],
    },
    methods: {
        setColorToCells: function() {
        }
    }
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
        console.log(app.cellsWithShips.battleship)
        for (let i = 0; i < app.cellsWithShips.battleship.length; i++) {
                       
        }
    })
    




