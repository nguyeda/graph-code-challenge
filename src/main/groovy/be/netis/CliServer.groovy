package be.netis

import be.netis.graph.Graph
import be.netis.io.SocketHandler
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Slf4j
@Component
class CliServer {

    private static final int PORT = 50000

    private final ServerSocket socketServer
    private final List<SocketHandler> clients = Collections.synchronizedList([])
    private final Graph graph
    private final BeanFactory beanFactory

    @Autowired
    CliServer(Graph graph, BeanFactory beanFactory) {
        this.graph = graph
        this.beanFactory = beanFactory
        this.socketServer = new ServerSocket(PORT)
    }

    @PostConstruct
    CliServer start() {
        Thread.start('cli-socket') {
            log.info 'starting socket server on port {}', this.socketServer.localPort
            while (true) {
                log.info 'waiting for new connection'
                socketServer.accept() { socket -> onIncomingConnection(socket) }
            }
        }
        this
    }

    CliServer stop() {
        if (!this.socketServer.isClosed()) {
            log.info 'stopping server'
            this.socketServer.close()
        }
        this
    }

    private void onIncomingConnection(Socket socket) {
        def handler = beanFactory.getBean(SocketHandler, socket)
        clients << handler
        log.info '{} connected clients', clients.size()

        try {
            handler.start()
        } catch (Throwable t) {
            log.error "error in client ${handler.session.id}, disconnecting", t
        } finally {
            handler.close()
            clients.remove(handler)
            log.info '{} connected clients', clients.size()
        }
    }
}
