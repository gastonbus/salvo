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
        getScoresData: function() {
            fetch('http://localhost:8080/api/games')
                .then(response => response.json())
                .then(data => {
                    app.games = data;
                    app.generatePlayersData();
                    console.log(app.players);
                });
        }
    },
    mounted: function() {
        this.getScoresData();
    }
})

function login(evt) {
  evt.preventDefault();
  var form = evt.target.form;
  $.post("/api/login", 
         { 
            userName: form["username"].value,
            password: form["password"].value
        })
   .done()
   .fail();
}

function logout(evt) {
  evt.preventDefault();
  $.post("/api/logout")
   .done()
   .fail();
}