package be.netis.io

import be.netis.CommandProcessor
import be.netis.graph.Graph
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
class SocketHandler {
    private static final int TIMEOUT_MS = 30000

    final SocketSessionWrapper session
    final BufferedReader reader

    @Autowired
    private Graph graph

    @Autowired
    private CommandProcessor commandProcessor

    SocketHandler(Socket socket) {
        socket.soTimeout = TIMEOUT_MS

        this.session = new SocketSessionWrapper(socket)
        this.reader = socket.inputStream.newReader()

        log.info 'new connection registered with session id [{}]', session.id
    }

    void start() {
        try {
            while (session.isConnected()) {
                log.debug '{} is waiting for command', session.id
                def buffer = reader.readLine()
                if (!commandProcessor.processCommand(buffer, session)) {
                    close()
                }
            }
        } catch (SocketTimeoutException e) {
            log.info 'timeout waiting for command in session [{}]', session.id
            close()
        }
    }

    void close() {
        if (session.isConnected()) {
            session.close()
        }
    }

    private class SocketSessionWrapper extends Session {
        final Socket delegate

        SocketSessionWrapper(Socket delegate) {
            this.delegate = delegate
            sendGreeting()
        }

        void send(String text) {
            delegate.outputStream << "$text\n"
        }

        boolean isConnected() {
            !delegate.isClosed()
        }
    }
}
