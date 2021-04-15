var app = new Vue({
    el: '#app',
    data: {
        games: [],
        players: []
    },
    methods: {
        isolatePlayers: function () {
            app.games.forEach(game => {
                game.gamePlayers.forEach(gamePlayer => {
                    if (!app.players.find(player => player.email == gamePlayer.player.email)) {
                        app.players.push({email: gamePlayer.player.email, scores: {totalScore: 0, wins: 0, losses: 0, ties: 0}});
                    }
                })
            })
        },
        generatePlayersData: function() {
            app.isolatePlayers();
            app.players.forEach(player => {
                app.games.forEach(game => {
                    game.gamePlayers.forEach(gamePlayer => {
                        if (gamePlayer.player.email == player.email) {
                            if (gamePlayer.score == 1) {
                                player.scores.wins++;
                            } else if (gamePlayer.score == 0) {
                                (player.scores.losses)++;
                            } else if (gamePlayer.score == 0.5) {
                                (player.scores.ties)++;
                            }
                            player.scores.totalScore += gamePlayer.score;
                        };
                    })
                })
            })
        },
        getScoresData: function() {
            fetch('http://localhost:8080/api/games')
                .then(response => response.json())
                .then(data => {
                    app.games = data;
                    app.generatePlayersData();
                });
        }
    },
    mounted: function() {
        this.getScoresData();
    }
})