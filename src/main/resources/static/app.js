var wsUri = "ws://localhost:8080/ws";
var websocket;

function connect() {
    websocket = new WebSocket(wsUri);

    websocket.onopen = onOpen;
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    websocket.onerror = onError;
}

function disconnect() {
    websocket.send('BYE MATE!');
}

function sendCommand() {
    doSend($('#command').val())
}

function teleport() {
    refreshGraph($('#teleport').val())
}

function onOpen() {
    writeToScreen('<span class="label label-default">Connected</span>');
}

function onClose() {
    writeToScreen('<span class="label label-warning">Disconnected</span>');
}

function onMessage(evt) {
    console.log("message: " + evt.data, evt);
    if (evt.data.startsWith('GRAPH')) {
        renderGraph(JSON.parse(evt.data.substring('GRAPH '.length)));
        writeToScreen('<span class="label label-success">REFRESH GRAPH</span>');
        return;
    }

    if (evt.data === 'EDGE ADDED' || evt.data === 'EDGE REMOVED') {
        var coordinates = $('#teleport').val();
        if (coordinates) {
            refreshGraph(coordinates);
        }
    }

    writeToScreen('<span class="label label-success">' + evt.data + '</span>');
}

function onError(evt) {
    console.log("error: ", evt);
    writeToScreen('<span class="label label-danger">Error: ' + evt.data + '</span>');
}

function doSend(message) {
    writeToScreen('<span class="label label-info">' + message + '</span>');
    websocket.send(message);
}

function refreshGraph(coordinates) {
    console.log('Refreshing graph for ' + coordinates);
    doSend('REFRESH GRAPH ' + coordinates);
}

function writeToScreen(message) {
    $('#output').append($('<p class="console-row">').html(message));
}
