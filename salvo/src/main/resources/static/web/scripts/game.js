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
        shipsPrevisualizationArray: [],
        playerAShipLocationsForPost: [],
        shipsSetted: false,
        salvoForPost: {
            turn: 0,
            locations: []
        },
        hits: [],
        playerRemainingShips: ["Carrier", "Battleship", "Submarine", "Destroyer", "Patrol Boat"],
        opponentRemainingShips: ["Carrier", "Battleship", "Submarine", "Destroyer", "Patrol Boat"],
    },
    methods: {
        savePlayers: function () {
            app.players.playerA = app.game.gamePlayers.find(elem => elem.gpid == app.gamePlayerId).player;
            if (app.game.gamePlayers.length == 2) {
                app.players.playerB = app.game.gamePlayers.find(elem => elem.gpid != app.gamePlayerId).player;
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
            app.game.hits.forEach(hit => {
                hit.locationsWithImpact.forEach(location => {
                    app.hits.push(location);
                })
            })
            app.playerASalvoes.forEach(salvo => {
                salvo.locations.forEach(location => {
                    document.getElementById('salvoes-' + location).innerText = salvo.turn;
                    if (app.hits.includes(location)) {
                        document.getElementById('salvoes-' + location).classList.add('successfulFiredSalvoes');                      
                    } else {
                        document.getElementById('salvoes-' + location).classList.add('firedSalvoes');                      
                    }
                })
            })
        },
        showOpponentSalvoes: function () {
            app.playerBSalvoes = app.game.salvoes.filter(elem => elem.playerId == app.players.playerB.id);
            app.playerBSalvoes.forEach(salvo => {
                salvo.locations.forEach(location => {
                    if (app.playerAShipsLocations.includes(location)) {
                    document.getElementById('ships-' + location).innerText = salvo.turn;
                    document.getElementById('ships-' + location).classList.add('opponentSuccessfulShot');
                    } else {
                        document.getElementById('ships-' + location).innerText = salvo.turn;
                        document.getElementById('ships-' + location).classList.add('opponentSalvoes');
                        }
                })
            })
        },
        getOpponentRemainingShips: function() {
            var lastTurn = 0;
            app.game.sunkShips.forEach(sunkShip => {
                if(sunkShip.turn > lastTurn) {
                    lastTurn = sunkShip.turn;
                }
            });
            if (app.game.sunkShips.length > 0) {
                app.game.sunkShips.find(sunk => sunk.turn == lastTurn).ships.forEach(ship => {
                    app.opponentRemainingShips = app.opponentRemainingShips.filter(s => s.toLowerCase().replace(" ", "") != ship.type);
                    ship.locations.forEach(location => {
                        document.getElementById('salvoes-' + location).className = 'commpletelySunk';
                    })
                });
            }
        },
        getPlayerRemainingShips: function() {
            var lastTurn = 0;
            app.game.opponentSunks.forEach(opponentSunkShip => {
                if(opponentSunkShip.turn > lastTurn) {
                    lastTurn = opponentSunkShip.turn;
                }
            });
            if (app.game.opponentSunks.length > 0) {
                app.game.opponentSunks.find(sunk => sunk.turn == lastTurn).ships.forEach(ship => {
                    app.playerRemainingShips = app.playerRemainingShips.filter(s => s.toLowerCase().replace(" ", "") != ship.type);
                    ship.locations.forEach(location => {
                        document.getElementById('ships-' + location).className = 'commpletelySunk';
                    });
                });
            }
        },
        getGamePlayerId: function () {
            const urlParams = new URLSearchParams(window.location.search);
            this.gamePlayerId = urlParams.get('gp');
        },
        getGameData: function () {
            fetch('/api/game_view/' + this.gamePlayerId)
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    // signal a server error to the chain
                    throw new Error(response.statusText);
                })
                .then(data => {
                    app.game = data;
                    app.shipsSetted = app.game.ships.length;
                    app.savePlayers();
                    app.locateShips();
                    app.showFiredSalvoes();
                    app.showOpponentSalvoes();
                    app.getPlayerRemainingShips();
                    app.getOpponentRemainingShips();
                    app.reload();

                })
                .catch(function (error) {
                    // called when an error occurs anywhere in the chain
                    alert("error en la petición de los datos al servidor");
                    console.log(error);
                    // window.location.href = "games.html";
                })
        },
        //generates an array (app.shipsPrevisualizationArray) with the Cell that have to be painted.
        extendSelectionOfShipLocation: function (event) {
            var currentCell = event.target.id.replace('ships-', "");
            var shipLength;
            app.shipsPrevisualizationArray = [];
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
            var columnNum = parseInt(currentCell.replace(/^./, ""), 10);
            var rowString = currentCell.charAt(0);
            var rowNum = parseInt(app.rows.indexOf(rowString)) + 1;
            if (app.radioSelection == "horizontal") {
                if (columnNum <= (10 - (shipLength - 1))) {
                    for (let i = 0; i < shipLength; i++) {
                        app.shipsPrevisualizationArray.push(rowString + columnNum);
                        columnNum++;
                    }
                }
            } else if (app.radioSelection == "vertical") {
                if (rowNum <= (10 - (shipLength - 1))) {
                    for (let i = 0; i < shipLength; i++) {
                        app.shipsPrevisualizationArray.push(app.rows[rowNum - 1] + columnNum);
                        rowNum++;
                    }
                }
            }
        },
        isShipPositionAvailable: function () {
            var available = true;
            app.playerAShipLocationsForPost.filter(ship => ship.type != app.comboBoxSelection).forEach(ship => {
                ship.locations.forEach(location => {
                    if (app.shipsPrevisualizationArray.includes(location)) {
                        available = false;
                    }
                })
            })
            return available;
        },
        //on mouseenter
        showShipLocationPrevisualization: function (event) {
            app.extendSelectionOfShipLocation(event);
            if (!app.playerAShipLocationsForPost.some(ship => ship.type == app.comboBoxSelection.toLowerCase())) {
                if (app.isShipPositionAvailable()) {
                    app.shipsPrevisualizationArray.forEach(currentCell => {
                        document.getElementById('ships-' + currentCell).classList.add(shipClass + 'Prev');
                    })
                }
            } else {
                if (app.isShipPositionAvailable()) {
                    app.shipsPrevisualizationArray.forEach(currentCell => {
                        document.getElementById('ships-' + currentCell).classList.add(shipClass + 'Prev');
                    })
                }
            }
        },
        //on mouseout
        removeShipLocationPrevisualization: function () {
            app.shipsPrevisualizationArray.forEach(currentCell => {
                document.getElementById('ships-' + currentCell).classList.remove(shipClass + 'Prev');
            })
        },
        //on click
        setShipLocation: function (event) {
            if (app.comboBoxSelection != "Select a ship") {
                if (!app.playerAShipLocationsForPost.some(ship => ship.type == app.comboBoxSelection.toLowerCase())) {
                    if (app.isShipPositionAvailable()) {
                        app.playerAShipLocationsForPost.push({
                            type: app.comboBoxSelection,
                            locations: app.shipsPrevisualizationArray
                        })
                        app.shipsPrevisualizationArray.forEach(currentCell => {
                            document.getElementById('ships-' + currentCell).classList = shipClass;
                        })
                    }
                } else {
                    if (app.isShipPositionAvailable()) {
                        var actualShip = app.playerAShipLocationsForPost.find(ship => ship.type == app.comboBoxSelection.toLowerCase());
                        actualShip.locations.forEach(currentCell => {
                            document.getElementById('ships-' + currentCell).classList.remove(shipClass)
                        })
                        app.shipsPrevisualizationArray.forEach(currentCell => {
                            document.getElementById('ships-' + currentCell).classList = shipClass;
                        })
                        actualShip.locations = app.shipsPrevisualizationArray;
                    }
                }
            }
        },
        clearGrid: function () {
            app.playerAShipLocationsForPost.forEach(ship =>
                ship.locations.forEach(location =>
                    document.getElementById('ships-' + location).className = ""));
            app.playerAShipLocationsForPost = [];
        },
        saveShips: function (gamePlayerId) {
            if (app.playerAShipLocationsForPost.length == 5) {
                $.post({
                        url: "/api/games/players/" + gamePlayerId + "/ships",
                        data: JSON.stringify(app.playerAShipLocationsForPost),
                        dataType: "text",
                        contentType: "application/json"
                    })
                    .done(function (response, status, jqXHR) {
                        window.location.href = "game.html?gp=" + gamePlayerId;
                    })
                    .fail(function (jqXHR, status, httpError) {
                        alert(JSON.parse(jqXHR.responseText).error);
                    })
            } else {
                alert("You have to locate all the ships.")
            }
        },
        isSalvoLocationAvailable: function (currentCell) {
            var available = true;
            if (app.salvoForPost.locations.includes(currentCell)) {
                available = false
            }

            app.playerASalvoes.forEach(salvo => {
                if (salvo.locations.includes(currentCell)) {
                    available = false
                }
            })
            return available;
        },
        showSalvoLocationPrevisualization: function (event) {
            if (app.shipsSetted && app.game.gameStatus == 'PLACE_SALVOES') {
                if (app.isSalvoLocationAvailable(event.target.id.replace('salvoes-', ""))) {
                    if (app.salvoForPost.locations.length < 5) {
                        document.getElementById(event.target.id).classList.add("shotPrev");
                    }
                }
            }

        },
        removeSalvoLocationPrevisualization: function (event) {
            document.getElementById(event.target.id).classList.remove("shotPrev");
        },
        setSalvoLocation: function (event) {
            var currentCell = event.target.id.replace('salvoes-', "");
            if (app.shipsSetted && app.game.gameStatus == 'PLACE_SALVOES') {
                if (app.salvoForPost.locations.includes(currentCell)) {
                    document.getElementById(event.target.id).classList.remove("shot");
                    app.salvoForPost.locations.splice(app.salvoForPost.locations.indexOf(currentCell), 1);
                } else {
                    if (app.isSalvoLocationAvailable(currentCell)) {
                        if (app.salvoForPost.locations.length < 5) {
                            document.getElementById(event.target.id).classList.add("shot");
                            app.salvoForPost.locations.push(currentCell);
                        }
                    }
                }
            }
        },
        saveSalvo: function (gamePlayerId) {
            if (app.salvoForPost.locations.length == 5) {
                
                app.salvoForPost.turn = app.playerASalvoes.length + 1;

                $.post({
                        url: "/api/games/players/" + gamePlayerId + "/salvoes",
                        data: JSON.stringify(app.salvoForPost),
                        dataType: "text",
                        contentType: "application/json"
                    })
                    .done(function (response, status, jqXHR) {
                        window.location.href = "game.html?gp=" + gamePlayerId;
                    })
                    .fail(function (jqXHR, status, httpError) {
                        alert(JSON.parse(jqXHR.responseText).error);
                    })
            } else {
                alert("You have to locate all the salvoes.")
            }
        },
        logout: function () {
            $.post("/api/logout")
                .done(function () {
                    app.authenticatedPlayer = null;
                    window.location.href = "games.html";
                })
        },
        reload: function () {
            if (this.game.gameStatus == "WAIT_OPPONENT") {
                setTimeout(this.getGameData(), 2000);
            }
        },
    },
    mounted: function () {
        this.getGamePlayerId();
        this.getGameData();
    },
});