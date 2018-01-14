package be.netis.graph

import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.ShortestPathSolver
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GraphEdgeSpec extends Specification {

    ShortestPathSolver shortestPathSolver = Mock(ShortestPathSolver)
    CloserThanSolver closerThanSolver = Mock(CloserThanSolver)
    Graph graph = new Graph(shortestPathSolver, closerThanSolver)
    List<Node> nodes

    def setup() {
        nodes = (1..10).collect { graph.addNode("node-$it") }.asImmutable()
    }

    def "add edge"() {
        given:
        def node1 = graph.getNode('node-1')
        def node2 = graph.getNode('node-2')

        expect:
        node1
        node2

        when:
        def edge = graph.addEdge('node-1', 'node-2', 1)

        then:
        edge.to == node2
        edge.weight == 1

        and:
        node1.edges == [edge]
    }

    def 'add duplicated edge is valid'() {
        given:
        def node1 = graph.getNode('node-1')
        def node2 = graph.getNode('node-2')
        def existingEdge = graph.addEdge('node-1', 'node-2', 2)

        when:
        def edge = graph.addEdge('node-1', 'node-2', 4)

        then:
        edge.to == node2
        edge.weight == 4

        and: 'new edge is not the same as the old edge'
        edge != existingEdge

        and:
        node1.edges == [existingEdge, edge]
    }

    def 'add edge from unknown node'() {
        when:
        graph.addEdge('node-1', 'unknown', 1)

        then:
        thrown(NodeNotFoundException)
    }

    def 'add edge to unknown node'() {
        when:
        graph.addEdge('unknown', 'node-2', 6)

        then:
        thrown(NodeNotFoundException)
    }

    def 'remove edges'() {
        given: 'some nodes'
        def node1 = graph.getNode('node-1')
        def node2 = graph.getNode('node-2')
        def node3 = graph.getNode('node-3')

        and: 'some edges'
        def edge1 = graph.addEdge('node-1', 'node-2', 2)
        def edge2 = graph.addEdge('node-1', 'node-2', 8)
        def edge3 = graph.addEdge('node-1', 'node-3', 3)
        def edge4 = graph.addEdge('node-2', 'node-1', 5)
        def edge5 = graph.addEdge('node-2', 'node-3', 1)

        expect:
        node1.edges == [edge1, edge2, edge3]
        node2.edges == [edge4, edge5]
        node3.edges.empty

        when:
        graph.removeEdge('node-1', 'node-2')

        then: 'all edges from node-1 to node-2 are removed'
        node1.edges == [edge3]

        and: 'other edges are unchanged'
        node2.edges == [edge4, edge5]
        node3.edges.empty
    }
}
