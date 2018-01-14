package be.netis.graph.solver

import be.netis.graph.Graph
import be.netis.graph.Node
import be.netis.graph.path.BreadthFirstCloserThanSolver
import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.ShortestPathSolver
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class BreadthFirstCloserThanSolverSpec extends Specification {

    CloserThanSolver closerThanSolver = new BreadthFirstCloserThanSolver()
    ShortestPathSolver shortestPathSolver = Mock(ShortestPathSolver)

    Graph graph = new Graph(shortestPathSolver, closerThanSolver)
    Map<String, Node> nodes


    def setup() {
        nodes = (1..10).collect { graph.addNode("node_$it") }.collectEntries { [it.name, it] }.asImmutable()
    }

    @Unroll
    def 'simple graph with no loop back [from: #from, maxDistance: #maxDistance]'() {
        given:
        graph.addEdge('node_1', 'node_2', 1)
        graph.addEdge('node_1', 'node_3', 4)
        graph.addEdge('node_1', 'node_6', 20)
        graph.addEdge('node_1', 'node_9', 3)
        graph.addEdge('node_2', 'node_4', 9)
        graph.addEdge('node_2', 'node_3', 5)
        graph.addEdge('node_3', 'node_4', 2)
        graph.addEdge('node_3', 'node_5', 11)
        graph.addEdge('node_4', 'node_5', 3)
        graph.addEdge('node_5', 'node_6', 1)
        graph.addEdge('node_5', 'node_8', 4)
        graph.addEdge('node_6', 'node_7', 10)
        graph.addEdge('node_7', 'node_8', 3)
        graph.addEdge('node_9', 'node_10', 42)

        when:
        def result = closerThanSolver.closerThan(maxDistance, nodes[from])

        then:
        result*.name == expectedNodes.collect { "node_$it" } as List<String>

        where:
        from     | maxDistance       | expectedNodes
        'node_1' | 0                 | []
        'node_1' | 3                 | [2]
        'node_2' | 4                 | []
        'node_1' | 5                 | [2, 3, 9]
        'node_1' | 8                 | [2, 3, 4, 9]
        'node_1' | 14                | [2, 3, 4, 5, 6, 8, 9]
        'node_1' | Integer.MAX_VALUE | [10, 2, 3, 4, 5, 6, 7, 8, 9]
    }

    def 'full mesh bidirectional graph'() {
        given:
        graph.addEdge('node_1', 'node_2', 1)
        graph.addEdge('node_1', 'node_3', 5)
        graph.addEdge('node_1', 'node_4', 10)

        graph.addEdge('node_2', 'node_1', 1)
        graph.addEdge('node_2', 'node_3', 1)
        graph.addEdge('node_2', 'node_4', 1)

        graph.addEdge('node_3', 'node_1', 1)
        graph.addEdge('node_3', 'node_2', 1)
        graph.addEdge('node_3', 'node_4', 1)

        graph.addEdge('node_4', 'node_1', 1)
        graph.addEdge('node_4', 'node_2', 1)
        graph.addEdge('node_4', 'node_3', 1)

        when:
        List<Node> result = closerThanSolver.closerThan(5, nodes['node_1'])

        then:
        result*.name == ['node_2', 'node_3', 'node_4']
    }

    @Unroll
    def 'complex graph [from: #from, maxDistance: #maxDistance]'() {
        given:
        graph.addEdge('node_1', 'node_2', 1)
        graph.addEdge('node_2', 'node_3', Integer.MAX_VALUE)
        graph.addEdge('node_2', 'node_4', 1)
        graph.addEdge('node_3', 'node_4', 1)
        graph.addEdge('node_4', 'node_3', 1)
        graph.addEdge('node_4', 'node_5', 11)
        graph.addEdge('node_5', 'node_1', 1)
        graph.addEdge('node_3', 'node_6', 1)
        graph.addEdge('node_3', 'node_6', Integer.MAX_VALUE)
        graph.addEdge('node_6', 'node_5', 1)
        graph.addEdge('node_6', 'node_7', 1)
        graph.addEdge('node_6', 'node_8', 10)
        graph.addEdge('node_6', 'node_9', Integer.MAX_VALUE)

        expect:
        closerThanSolver.closerThan(maxDistance, nodes[from])*.name == expectedNodes.collect {
            "node_$it"
        } as List<String>

        where:
        from     | maxDistance       | expectedNodes
        'node_1' | 10                | [2, 3, 4, 5, 6, 7]
        'node_2' | 10                | [1, 3, 4, 5, 6, 7]
        'node_2' | 6                 | [1, 3, 4, 5, 6, 7]
        'node_2' | Integer.MAX_VALUE | [1, 3, 4, 5, 6, 7, 8]
    }
}
