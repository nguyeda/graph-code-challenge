package be.netis.graph

import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.ShortestPathSolver
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.util.concurrent.ConcurrentHashMap

@Slf4j
@Component
class Graph {

    private final Map<String, Node> nodes
    private final ShortestPathSolver shortestPathSolver
    private final CloserThanSolver closerThanSolver

    @Autowired
    Graph(ShortestPathSolver shortestPathSolver, CloserThanSolver closerThanSolver) {
        this.nodes = new ConcurrentHashMap()
        this.shortestPathSolver = shortestPathSolver
        this.closerThanSolver = closerThanSolver
    }

    @PostConstruct
    private void init() {
//        (1..10).each { addNode("node-$it") }
//        addEdge('node-1', 'node-2', 1)
//        addEdge('node-1', 'node-3', 4)
//        addEdge('node-1', 'node-6', 20)
//        addEdge('node-1', 'node-9', 3)
//        addEdge('node-2', 'node-4', 9)
//        addEdge('node-2', 'node-3', 5)
//        addEdge('node-3', 'node-4', 2)
//        addEdge('node-3', 'node-5', 11)
//        addEdge('node-4', 'node-5', 3)
//        addEdge('node-5', 'node-6', 1)
//        addEdge('node-5', 'node-8', 4)
//        addEdge('node-6', 'node-7', 10)
//        addEdge('node-7', 'node-8', 3)
//        addEdge('node-9', 'node-10', 8)
    }

    List<Node> getNodes() {
        nodes.values().asList().asImmutable()
    }

    Node addNode(String name) throws NodeAlreadyExistsException {
        NodeAlreadyExistsException.throwIf(nodes.containsKey(name))

        log.debug 'adding node {}', name
        def node = new Node(name: name)
        nodes.put(name, node)

        return node
    }

    Node removeNode(String name) throws NodeNotFoundException {
        NodeNotFoundException.throwIf(!nodes.containsKey(name), name)

        log.debug 'removing node {}', name
        nodes.remove(name)
    }

    Edge addEdge(String fromNodeName, String toNodeName, int weight) throws NodeNotFoundException {
        def fromNode = getNode(fromNodeName)
        def toNode = getNode(toNodeName)

        log.debug 'adding edge between nodes [{}, {}] with weight {}', fromNodeName, toNodeName, weight
        def edge = new Edge(to: toNode, weight: weight)
        fromNode.addEdge(edge)

        return edge
    }

    void removeEdge(String fromNodeName, String toNodeName) throws NodeNotFoundException {
        def fromNode = getNode(fromNodeName)
        def toNode = getNode(toNodeName)

        log.debug 'removing edges between nodes [{}, {}]', fromNodeName, toNodeName
        fromNode.removeEdges(toNode)
    }

    int shortestPath(String fromNodeName, String toNodeName) throws NodeNotFoundException {
        def fromNode = getNode(fromNodeName)
        def toNode = getNode(toNodeName)

        log.debug 'graph size {}', nodes.size()
        shortestPathSolver.shortestPath(fromNode, toNode).totalDistance
    }

    List<Node> closerThan(int weight, String fromNodeName) throws NodeNotFoundException {
        def fromNode = getNode(fromNodeName)

        log.debug 'graph size {}', nodes.size()
        closerThanSolver.closerThan(weight, fromNode)
    }

    Node getNode(String name) throws NodeNotFoundException {
        def node = nodes[name]
        NodeNotFoundException.throwIf(!node, name)
        return node
    }
}
