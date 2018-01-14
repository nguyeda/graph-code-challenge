package be.netis.io

import be.netis.CommandProcessor
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ConcurrentHashMap

@Slf4j
@Component
class WebSocketHandler extends TextWebSocketHandler {

    final Map<WebSocketSession, WebSocketSessionWrapper> sessions = new ConcurrentHashMap<>()
    final CommandProcessor commandProcessor

    @Autowired
    WebSocketHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor
        commandProcessor.registerBroadcastListener { String message, List<Session> excludes ->
            sessions.values()
                    .findAll { !excludes.contains(it) }
                    .each { it.send message }
        }
    }

    @Override
    void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        def wrapper = sessions.get(session)
        Objects.requireNonNull(wrapper, 'Session wrapper not found')

        if (!commandProcessor.processCommand(message.payload.trim(), wrapper)) {
            wrapper.close()
        }
    }

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session, new WebSocketSessionWrapper(session))
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session)
    }

    private class WebSocketSessionWrapper extends Session {
        final WebSocketSession delegate

        WebSocketSessionWrapper(WebSocketSession delegate) {
            this.delegate = delegate
            sendGreeting()
        }

        void send(String text) {
            delegate.sendMessage(new TextMessage(text))
        }
    }
}
