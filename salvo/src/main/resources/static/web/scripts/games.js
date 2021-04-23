var app = new Vue({
    el: '#app',
    data: {
        games: [],
        authenticatedPlayer: [],
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
        login: function () {
            $.post("/api/login", {
                    userName: app.userName,
                    password: app.password
                })
                .done(function () {
                    // alert("Has ingresado correctamente.");
                    location.reload()
                })
                .fail(function () {
                    alert("Hubo un error al intentar iniciar sesión.")
                })
        },
        signup: function () {
            // $.post("/api/players", { userName: "gaston@gmail.com", password: "1234" })
            $.post("/api/players", {
                    userName: app.userName,
                    password: app.password
                })
                .done(function () {
                    app.login();
                    alert("Jugador registrado exitosamente.")
                    console.log("Jugador registrado exitosamente.");
                })
                .fail(function () {
                    alert("Hubo un error al intentar crear el usuario.")
                })
        },
        logout: function () {
            $.post("/api/logout")
                .done(function () {
                    app.authenticatedPlayer = null;
                    // alert("Usted ha salido.")
                    location.reload();
                    // console.log("logged out")
                })
        }
    },
    mounted: function () {
        this.getScoresData();
    }
})