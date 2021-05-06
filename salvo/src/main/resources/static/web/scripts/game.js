var app = new Vue({
    el: '#app',
    data: {
        gamePlayerId: null,
        game: {},
        rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
        columns: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
        players: {
            playerA: {},
            playerB: {},
        },
        playerASalvoes: null,
        playerBSalvoes: null,
        playerAShipsLocations: [],
        comboBoxSelection: "Select a ship",
        radioSelection: "horizontal",
        shipClass: "",
        // shipTypes: ["carrier", "battleship", "submarine", "destroyer", "patrolboat"],
        previsualizationArray: [],
        playerAShipLocationsForPost: [],
        shipsSetted: false,
    },
    methods: {
        savePlayers: function () {
            app.players.playerA = app.game.gamePlayers.find(element => element.gpid == app.gamePlayerId).player;
            if (app.game.gamePlayers.length == 2) {
                app.players.playerB = app.game.gamePlayers.find(element => element.gpid != app.gamePlayerId).player;
            }
        },
        locateShips: function () {
            app.game.ships.forEach(ship => {
                app.playerAShipsLocations = app.playerAShipsLocations.concat(ship.locations);
                ship.locations.forEach(location => {
                    document.getElementById('ships-' + location).className = ship.type.toLowerCase().replace(" ", "");
                });
            });
        },
        showFiredSalvoes: function () {
            app.playerASalvoes = app.game.salvoes.filter(elem => elem.playerId == app.players.playerA.id);
            app.playerASalvoes.forEach(salvo => {
                salvo.locations.forEach(location => {
                    document.getElementById('salvoes-' + location).innerHTML = "<div class='firedSalvoes'>" + salvo.turn + "</div>";
                })
            })
        },
        showOpponentSalvoes: function () {
            app.playerBSalvoes = app.game.salvoes.filter(elem => elem.playerId == app.players.playerB.id);
            app.playerBSalvoes.forEach(salvo => {
                salvo.locations.forEach(location => {
                    if (app.playerAShipsLocations.includes(location)) {
                        document.getElementById('ships-' + location).innerHTML = "<div class='opponentSuccessfulShot'>" + salvo.turn + "</div>";
                    } else {
                        document.getElementById('ships-' + location).innerHTML = "<div class='opponentSalvoes'>" + salvo.turn + "</div>";
                    }
                })
            })
        },
        getGamePlayerId: function () {
            const urlParams = new URLSearchParams(window.location.search);
            this.gamePlayerId = urlParams.get('gp');
        },
        getGameData: function () {
            fetch('http://localhost:8080/api/game_view/' + this.gamePlayerId)
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    // signal a server error to the chain
                    throw new Error(response.statusText);
                })
                .then(data => {
                    app.game = data;
                    app.savePlayers();
                    app.locateShips();
                    app.showFiredSalvoes();
                    app.showOpponentSalvoes();
                })
                .catch(function (error) {
                    // called when an error occurs anywhere in the chain
                    alert("error en la petición de los datos al servidor");
                    console.log(error);
                    window.location.href = "games.html";
                })
        },
        extendSelectionOfShipLocation: function (event) {
            var cell = event.target.id;
            var shipLength;
            app.previsualizationArray = [];
            switch (app.comboBoxSelection) {
                case "carrier":
                    shipLength = 5;
                    shipClass = "carrier";
                    break;
                case "battleship":
                    shipLength = 4;
                    shipClass = "battleship";
                    break;
                case "submarine":
                    shipLength = 3;
                    shipClass = "submarine";
                    break;
                case "destroyer":
                    shipLength = 3;

                    shipClass = "destroyer";
                    break;
                case "patrolboat":
                    shipLength = 2;
                    shipClass = "patrolboat";
                    break;
            }
            var columnNum = parseInt(cell.replace(/^./, ""), 10);
            var rowString = cell.charAt(0);
            var rowNum = parseInt(app.rows.indexOf(rowString)) + 1;
            if (app.radioSelection == "horizontal") {
                if (columnNum <= (10 - (shipLength - 1))) {
                    for (let i = 0; i < shipLength; i++) {
                        app.previsualizationArray.push(rowString + columnNum);
                        columnNum++;
                    }
                }
            } else if (app.radioSelection == "vertical") {
                if (rowNum <= (10 - (shipLength - 1))) {
                    for (let i = 0; i < shipLength; i++) {
                        app.previsualizationArray.push(app.rows[rowNum - 1] + columnNum);
                        rowNum++;
                    }
                }
            }
            //generates an array (app.previsualizationArray) with the cells that have to be painted.
        },
        isShipPositionAvailable: function () {
            var available = true;
            app.playerAShipLocationsForPost.filter(ship => ship.type != app.comboBoxSelection).forEach(ship => {
                ship.locations.forEach(location => {
                    if (app.previsualizationArray.includes(location)) {
                        available = false;
                    }
                })
            })
            return available;
        },
        showShipLocationPrevisualization: function (event) {
            app.extendSelectionOfShipLocation(event);
            if (!app.playerAShipLocationsForPost.some(ship => ship.type == app.comboBoxSelection.toLowerCase())) {
                if (app.isShipPositionAvailable()) {
                    app.previsualizationArray.forEach(cell => {
                        document.getElementById(cell).classList.add(shipClass + 'Prev');
                    })
                }
            } else {
                if (app.isShipPositionAvailable()) {
                    app.previsualizationArray.forEach(cell => {
                        document.getElementById(cell).classList.add(shipClass + 'Prev');
                    })
                }
            }
        },
        removeShipLocationPrevisualization: function () {
            app.previsualizationArray.forEach(cell => {
                document.getElementById(cell).classList.remove(shipClass + 'Prev');
            })
        },
        setShipLocation: function (
            // event
        ) {
            if (!app.playerAShipLocationsForPost.some(ship => ship.type == app.comboBoxSelection.toLowerCase())) {
                if (app.isShipPositionAvailable()) {
                    app.playerAShipLocationsForPost.push({
                        type: app.comboBoxSelection,
                        locations: app.previsualizationArray
                    })
                    app.previsualizationArray.forEach(cell => {
                        document.getElementById(cell).classList.add(shipClass);
                    })
                }
            } else {
                if (app.isShipPositionAvailable()) {
                    var actualShip = app.playerAShipLocationsForPost.find(ship => ship.type == app.comboBoxSelection.toLowerCase());
                    actualShip.locations.forEach(cell => {
                        document.getElementById(cell).classList.remove(shipClass)
                    })
                    app.previsualizationArray.forEach(cell => {
                        document.getElementById(cell).classList.add(shipClass);
                    })
                    actualShip.locations = app.previsualizationArray;
                }
            }
        },
        clearGrid: function() {
            // procedure to clear the grid:
            // app.playerAShipLocationsForPost = [];
            // document.getElementsByClassName("clear").classList.remove("carrier");
            // document.getElementsByClassName("clear").classList.remove("battleship");
            // document.getElementsByClassName("clear").classList.remove("submarine");
            // document.getElementsByClassName("clear").classList.remove("destroyer");
            // document.getElementsByClassName("clear").classList.remove("patrolboat");
        },
       saveShips: function (gamePlayerId) {
            $.post({
                    url: "/api/games/players/" + gamePlayerId + "/ships",
                    data: JSON.stringify([
                        {
                            type: "carrier", 
                            locations: ["C4", "D4", "E4", "F4", "G4"]
                        },        
                        {
                            type: "battleship",
                            locations: ["E6", "E7", "E8", "E9"]
                        },
                        {
                            type: "submarine",
                            locations: ["G1", "H1", "I1"]
                        },
                        {
                            type: "destroyer",
                            locations: ["J5", "J6", "J7"]
                        },
                        {
                            type: "patrolboat",
                            locations: ["H10", "I10"]
                        }
                    ]
                    ),
                    dataType: "text",
                    contentType: "application/json"
                })
                //if successful
                .done(function (response, status, jqXHR) {
                    //recargar pagina (si es que está en game.html, y sino ir a game.html?gp=xx)
                    //tener cuidado de que tambien tiene que haber terminado de cargar sus ships el oponente
                    // alert( "Ships added: " + response );
                    app.shipsSetted = true;
                    window.location.href = "game.html?gp=" + gamePlayerId;
                })
                .fail(function (jqXHR, status, httpError) {
                    alert("Failed to add ships: " + httpError);
                })
        },
        logout: function () {
            $.post("/api/logout")
            .done(function () {
                app.authenticatedPlayer = null;
                // alert("Usted ha salido.")
                window.location.href = "games.html";
                // console.log("logged out")
            })
        },
    },
    mounted: function () {
        this.getGamePlayerId();
        this.getGameData();
    },
});