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
                setSelection: function (index) {
                    this.selected = index;
                },
                selectionDown: function () {
                    if (this.selected < this.displayServers.length) {
                        this.selected++
                    }
                },
                selectionUp: function () {
                    if (this.selected > 0) {
                        this.selected--
                    }
                },
                enter: function () {
                    var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/add/";
                    if (this.displayServers.length !== 0) {
                        if (this.displayServers.length === this.selected) {
                            this.add();
                        } else {
                            url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/server/" + this.displayServers[this.selected] + "/";
                        }
                    } else {
                        this.add();
                    }
                    window.location = url;
                },
                go: function (event) {
                    var domain = event.target.innerText;
                    var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/server/" + domain + "/";
                    window.location = url;
                },
                add: function () {
                    var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/add/";
                    window.location = url;
                }
            }
    });
}