var app = new Vue({
    el: '#app',
    data: {
        games: [],
        authenticatedPlayer: null,
        players: [],
        userName: "",
        password: ""
    },
    methods: {
        isolatePlayers: function () {
            app.games.forEach(game => {
                game.gamePlayers.forEach(gamePlayer => {
                    if (!app.players.find(player => player.email == gamePlayer.player.email)) {
                        app.players.push({
                            email: gamePlayer.player.email,
                            scores: {
                                totalScore: 0,
                                wins: 0,
                                losses: 0,
                                ties: 0
                            }
                        });
                    }
                })
            })
        },
        generatePlayersData: function () {
            app.isolatePlayers();
            app.players.forEach(player => {
                app.games.forEach(game => {
                    game.gamePlayers.forEach(gamePlayer => {
                        if (gamePlayer.player.email == player.email) {
                            if (gamePlayer.score == 1) {
                                player.scores.wins++;
                            } else if (gamePlayer.score == 0) {
                                player.scores.losses++;
                            } else if (gamePlayer.score == 0.5) {
                                player.scores.ties++;
                            }
                            player.scores.totalScore += gamePlayer.score;
                        };
                    })
                })
            })
        },
        getScoresData: function () {
            fetch('http://localhost:8080/api/games')
                .then(response => response.json())
                .then(data => {
                    app.games = data.games;
                    app.authenticatedPlayer = data.player;
                    app.generatePlayersData();
                    // console.log(app.players);
                });
        },
        createGame: function() {
            $.post("/api/games")
            .done(function (data) {
                alert("Game created! Press accept to play.");
                window.location.href = "game.html?gp=" + data.gpid;
            })
            .fail(function () {
                alert("There was a problem creating a new game.");
            })
        },
        joinGame: function(gameId) {
            $.post("/api/games/" + gameId + "/players")
            .done(function (data) {
                window.location.href = "game.html?gp=" + data.gpid;
                // alert("Congratulations! You've just joined this game. Good luck!");
            })
            .fail(function () {
                alert("There was a problem joining the game.");
            })
        },
        login: function () {
            $.post("/api/login", {
                    userName: app.userName,
                    password: app.password
                })
                .done(function () {
                    // alert("Logged in successfully!");
                    location.reload();
                })
                .fail(function () {
                    alert("There was an error trying to start session.");
                })
        },
        signup: function () {
            $.post("/api/players", {
                    userName: app.userName,
                    password: app.password
                })
                .done(function () {
                    app.login();
                    alert("Player created successfully.");
                    // console.log("Player created successfully.");
                })
                .fail(function () {
                    alert("There was an error creating the player.");
                })
        },
        logout: function () {
            $.post("/api/logout")
                .done(function () {
                    app.authenticatedPlayer = null;
                    // alert("Logged out");
                    location.reload();
                    // console.log("logged out");
                })
        }
    },
    mounted: function () {
        this.getScoresData();
    }
})