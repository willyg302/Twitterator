var feedSocket;

$(function() {
    feedSocket = {
        socket: null,
        init: function() {
            this.socket = new WebSocket('ws://127.0.0.1:8081/');
            this.socket.onopen = this.open;
            this.socket.onclose = this.close;
            this.socket.onmessage = this.message;
            this.socket.onerror = this.error;
        },
        open: function() {
            console.log('open');
        },
        close: function() {
            console.log('close');
        },
        message: function(message) {
            
            document.getElementById("sdivChat").innerHTML += message.data + "<br>";
            
        //$('sdivChat').innerHTML += $('<li />').html(message.data);
        //var alert = eval('(' + message.data + ')');
        //$('#alerts-feed').append(
        //    $('<li />').html(message.data)
        //    )
        },
        error: function(e) {
            console.log(e);
        }
    };

    feedSocket.init();
});

function sendMessage(msg) {
    document.getElementById("sdivChat").innerHTML += "Sending Message: " + msg + "<br>";
    feedSocket.socket.send(msg);
}


/**
 * Regular HTTP AJAX stuff follows
 * This forces the server to use our AJAX polling handler, which is the
 * "default" if everything else fails. Very hacky.
 */
function ajaxPoll(value) {
    var req;
    if (typeof XMLHttpRequest != "undefined") {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    req.open("GET", "bogus.html?query=" + value, true);
    req.onreadystatechange = function() {ajaxCallback(req)};
    req.send(null);
}

function ajaxCallback(req) {
    if (req.readyState == 4 && req.status == 200) {
        //var locations = req.responseXML.getElementsByTagName("location")[0].childNodes[0].nodeValue;
        //var messages = req.responseXML.getElementsByTagName("message")[0].childNodes[0].nodeValue;
        
        var locations = req.responseXML.getElementsByTagName("location");
        var messages = req.responseXML.getElementsByTagName("message");
        for (var i = 0; i < locations.length; i++) {
            var location = locations[i].childNodes[0].nodeValue;
            var message = messages[i].childNodes[0].nodeValue;
            document.getElementById(location).innerHTML = message;
        }
        
        
        
    }
}

function startup() {
    ajaxPoll("startup");
}

function sendPrompt(action, prompt, db) {
    ajaxPoll("omnibox&action=" + action + "&prompt=" + prompt + "&db=" + db);
}