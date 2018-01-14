package be.netis

import be.netis.graph.Graph
import be.netis.graph.Node
import be.netis.graph.NodeAlreadyExistsException
import be.netis.graph.NodeNotFoundException
import be.netis.io.BroadcastListener
import be.netis.io.GraphModel
import be.netis.io.Session
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.regex.Matcher

@Slf4j
@Component
class CommandProcessor {

    final Graph graph
    private final List<BroadcastListener> broadcastListeners = []

    @Autowired
    CommandProcessor(Graph graph) {
        this.graph = graph
    }

    void registerBroadcastListener(BroadcastListener listener) {
        broadcastListeners << listener
    }

    boolean processCommand(String command, Session session) {
        log.debug '[{}] -> {}', session.id, command

        switch (command) {
            case ~/(?i)hi, I'm ([a-zA-Z0-9\\-]+)/:
                setName(Matcher.lastMatcher[0][1] as String, session)
                return true

            case ~/(?i)add node ([a-zA-Z0-9\\-]+)/:
                addNode(Matcher.lastMatcher[0][1] as String, session)
                return true

            case ~/(?i)remove node ([a-zA-Z0-9\\-]+)/:
                removeNode(Matcher.lastMatcher[0][1] as String, session)
                return true

            case ~/(?i)add edge ([a-zA-Z0-9\\-]+) ([a-zA-Z0-9\\-]+) ([0-9]+)/:
                def groups = Matcher.lastMatcher[0]
                addEdge(groups[1] as String, groups[2] as String, groups[3] as int, session)
                return true

            case ~/(?i)remove edge ([a-zA-Z0-9\\-]+) ([a-zA-Z0-9\\-]+)/:
                def groups = Matcher.lastMatcher[0]
                removeEdge(groups[1] as String, groups[2] as String, session)
                return true

            case ~/(?i)shortest path ([a-zA-Z0-9\\-]+) ([a-zA-Z0-9\\-]+)/:
                def groups = Matcher.lastMatcher[0]
                shortestPath(groups[1] as String, groups[2] as String, session)
                return true

            case ~/(?i)closer than ([0-9]+) ([a-zA-Z0-9\\-]+)/:
                def groups = Matcher.lastMatcher[0]
                closerThan(groups[1] as int, groups[2] as String, session)
                return true

            case ~/(?i)refresh graph ([a-zA-Z0-9\\-]+) ([0-9]+)/:
                def groups = Matcher.lastMatcher[0]
                refreshGraph(groups[1] as String, groups[2] as int, session)
                return true

            case ~/(?i)BYE MATE!/:
            case ~/(?i)bye/:
                return false

            default:
                log.warn 'unknown command [{}]', command
                session.send 'SORRY, I DIDN\'T UNDERSTAND THAT'
                return true
        }
    }

    private void setName(String name, Session session) {
        log.debug 'user in session [{}] is called {}', session.id, name
        session.name = name
        session.send "HI $name"
    }

    private void addNode(String name, Session session) {
        try {
            graph.addNode(name)
            send session, 'NODE ADDED', true
        } catch (NodeAlreadyExistsException e) {
            log.warn 'Node {} is already registered', name
            session.send 'ERROR: NODE ALREADY EXISTS'
        }
    }

    private void removeNode(String name, Session session) {
        try {
            graph.removeNode(name)
            send session, 'NODE REMOVED', true
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void addEdge(String fromNodeName, String toNodeName, int weight, Session session) {
        try {
            graph.addEdge(fromNodeName, toNodeName, weight)
            send session, 'EDGE ADDED', true
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void removeEdge(String fromNodeName, String toNodeName, Session session) {
        try {
            graph.removeEdge(fromNodeName, toNodeName)
            send session, 'EDGE REMOVED', true
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void shortestPath(String fromNodeName, String toNodeName, Session session) {
        try {
            int weight = graph.shortestPath(fromNodeName, toNodeName)
            send session, weight as String
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void closerThan(int weight, String fromNodeName, Session session) {
        try {
            List<Node> nodes = graph.closerThan(weight, fromNodeName)
            send session, nodes*.name.join(',')
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void refreshGraph(String fromNodeName, int weight, Session session) {
        try {
            List<Node> nodes = [graph.getNode(fromNodeName)] + graph.closerThan(weight, fromNodeName)
            session.send "GRAPH ${JsonOutput.toJson(GraphModel.map(nodes))}"
        } catch (NodeNotFoundException e) {
            log.warn 'Node {} does not exist', e.name
            session.send 'ERROR: NODE NOT FOUND'
        }
    }

    private void send(Session session, String message, boolean broadcast = false) {
        session.send(message)
        if (broadcast) {
            broadcastListeners.each { it.sendAll(message, [session]) }
        }
    }
}
