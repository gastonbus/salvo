var app = new Vue({
    el: '#app',
    data: {
        games: [],
  }
});

fetch('http://localhost:8080/api/games')
    .then(response => response.json())
    .then(data => app.games = data);



