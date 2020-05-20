function createVueApp(serverParam) {
    var app = new Vue({
        el: '#search_box',
        data: function () {
            return {
                filter: null,
                allServers: serverParam,
                selected: 0
            }
        },
        computed: {
            displayServers: function () {
                //Show first 10 matching servers
                var temp = [];
                for (var i = 0; i < this.allServers.length; i++) {
                    var server = this.allServers[i];
                    var regex = RegExp(this.filter, "i");
                    if (regex.test(server)) {
                        temp.push(server);
                    }
                    if (temp.length == 10) break;
                }
                return temp;
            }
        },
        methods:
            {
                selectAdd: function (index) {
                    this.selected = this.displayServers.length;
                },
                selectCheck: function (index) {
                    this.selected = this.displayServers.length + 1;
                },
                setSelection: function (index) {
                    this.selected = index;
                },
                selectionDown: function () {
                    if (this.selected <= this.displayServers.length) {
                        this.selected++
                    }
                },
                selectionUp: function () {
                    if (this.selected > 0) {
                        this.selected--
                    }
                },
                getBaseUrl: function () {
                    return window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
                },
                enter: function () {
                    if (this.selected === this.displayServers.length ) {
                        this.add();
                    } else if (this.selected === this.displayServers.length + 1) {
                        this.goInput();
                    } else {
                        this.goToResultPage(this.displayServers[this.selected]);
                    }
                },
                add: function (domain) {
                    this.goToPath("/add/");
                },
                goInput: function () {
                    this.goToResultPage(document.getElementById("search_field").value);
                },
                goSuggestion: function (event) {
                    this.goToResultPage(event.target.innerText);
                },
                goToResultPage: function (domain) {
                    this.goToPath("/server/" + domain + "/");
                },
                goToPath: function (path) {
                    window.location = this.getBaseUrl() + path;
                }
            }
    });
}