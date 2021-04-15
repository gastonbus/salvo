var app = new Vue({
    el: '#app',
    data: {
        games: [],
        gamePlayers: [],
        players: [],
    },
    methods: {
        isolatePlayers: function() {
            var playerEmails = [];
            app.games.forEach(game => {
                game.gamePlayers.forEach(gamePlayer => {
                    if (!playerEmails.includes(gamePlayer.player.email)) {
                        playerEmails.push(gamePlayer.player.email);
                        app.players.push({
                                email: gamePlayer.player.email,
                                scores: {
                                    totalScore: null,
                                    wins: null,
                                    loses: null,
                                    ties: null
                                } 
                        });
                    }    
                })           
            })
        },
        calculateTotalScore: function() {
            var playerEmails = app.players.map(player => player.email);
            for (let i = 0; i < playerEmails.length; i++) {
                app.players[i].scores.totalScore = app.games.map(game => game.gamePlayers).reduce((gamePlayers,gamePlayer) => gamePlayers.concat(gamePlayer)).filter(gamePlayer => gamePlayer.player.email == playerEmails[i]).map(gamePlayer => gamePlayer.score).reduce((totalScore, playerScore) => totalScore + playerScore);
            }
        },
        calculateResults: function(resultType) {
            var playerEmails = app.players.map(player => player.email);
            for (let i = 0; i < playerEmails.length; i++) {
                var counter = 0;
                for (let j = 0; j < app.games.length; j++) {
                    for (let k = 0; k < app.games[j].gamePlayers.length; k++) {
                        if (app.games[j].gamePlayers[k].score == (resultType == "wins" ? 1 : resultType == "ties" ? 0.5 : 0) && app.games[j].gamePlayers[k].player.email == playerEmails[i]) {
                            counter++;
                        };
                    }
                }
                if (resultType == "wins") {
                    app.players[i].scores.wins = counter;
                } else if (resultType == "ties") {
                    app.players[i].scores.loses = counter;
                } else {
                    app.players[i].scores.ties = counter;
                }
            }
        },
    }    
})

fetch('http://localhost:8080/api/games')
    .then(response => response.json())
    .then(data => {
        app.games = data;
        app.isolatePlayers();
        app.calculateTotalScore();
        app.calculateResults("wins");
        app.calculateResults("loses");
        app.calculateResults("ties");
        console.log(app.players);
    });