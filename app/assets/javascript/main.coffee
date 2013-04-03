clientId = Math.random().toString().substring(2,6)
$("h2").append(clientId)
echoSocket = new WebSocket("ws://"+location.host+"/echo/"+clientId)
connected = false
echoSocket.onopen = () ->
	connected = true
	console.log "connected"

echoSocket.onclose = () ->
	connected = false
	console.log "disconnected"

echoSocket.onmessage = (event) ->
	event = JSON.parse(event.data)
	$.each(event, (k, v) -> 
		if k == "clear"
			$txt = $("#txt").val("")
			$("#output").html("")
		if k == "message"
			$("#output").append("<li>" + event.message + "</li>")
	)

$("#send").click((e) ->
	e.preventDefault()
	if connected
		$txt = $("#txt")
		echoSocket.send(JSON.stringify {"message": $txt.val()})
		$txt.val("")
)

$("#clear").click((e) -> 
	if connected
		echoSocket.send(JSON.stringify {"clear": true})
)