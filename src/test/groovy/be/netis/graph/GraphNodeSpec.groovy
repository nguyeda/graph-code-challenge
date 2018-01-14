package be.netis.graph

import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.ShortestPathSolver
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GraphNodeSpec extends Specification {

    ShortestPathSolver shortestPathSolver = Mock(ShortestPathSolver)
    CloserThanSolver closerThanSolver = Mock(CloserThanSolver)
    Graph graph = new Graph(shortestPathSolver, closerThanSolver)

    def "add node to the graph"() {
        given:
        def nodeName = 'azerty'

        when:
        def node = graph.addNode(nodeName)

        then:
        node.name == nodeName

        and:
        graph.nodes == [node]
    }

    def 'adding node with registered name throws an exception'() {
        given:
        def nodeName = 'azerty'
        def node = graph.addNode(nodeName)

        when:
        graph.addNode(nodeName)

        then:
        thrown(NodeAlreadyExistsException)

        and: 'node was not added'
        graph.nodes == [node]
    }

    def 'retrieve node from graph'() {
        given:
        def nodeName = 'azerty'
        def expected = graph.addNode(nodeName)

        when:
        def node = graph.getNode(nodeName)

        then:
        node == expected
    }

    def 'retrieve unknown node from graph throws an exception'() {
        when:
        graph.getNode('unknown')

        then:
        thrown(NodeNotFoundException)
    }

    def "remove node from graph"() {
        given:
        def nodeName = 'node-3'
        (1..10).each { graph.addNode("node-$it") }

        when:
        def node = graph.removeNode(nodeName)

        then:
        node.name == nodeName

        and: 'node has been removed from the graph'
        graph.nodes.size() == 9
        !graph.nodes.find { it.name == nodeName }
    }

    def 'removing unknown node throws an exception'() {
        given:
        def nodeName = 'unknown'
        def nodes = (1..10).collect { graph.addNode("node-$it") }.asImmutable()

        when:
        graph.removeNode(nodeName)

        then:
        thrown(NodeNotFoundException)

        and:
        graph.nodes.size() == nodes.size()
        graph.nodes.containsAll(nodes)
    }
}
