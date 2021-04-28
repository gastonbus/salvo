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
        getGamePlayerId: function() {
            const urlParams = new URLSearchParams(window.location.search);
            this.gamePlayerId = urlParams.get('gp');
        },
        getGameData: function() {
            fetch('http://localhost:8080/api/game_view/' + this.gamePlayerId)
                .then(response => {
                    if (response.ok) {
                        // add a new promise to the chain
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
                .catch(function(error) {
                    // called when an error occurs anywhere in the chain
                    alert("This is not your game. Don't try to cheat!");
                    console.log(error);
                    window.location.href("games.html");
                })
        },
        logout: function () {
            $.post("/api/logout")
                .done(function () {
                    app.authenticatedPlayer = null;
                    // alert("Usted ha salido.")
                    window.location.href= "games.html";
                    // console.log("logged out")
                })
        }
    },
    mounted: function() {
        this.getGamePlayerId();
        this.getGameData();
    }
});
