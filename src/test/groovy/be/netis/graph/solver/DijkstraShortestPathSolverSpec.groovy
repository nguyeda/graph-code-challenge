package be.netis.graph.solver

import be.netis.graph.Graph
import be.netis.graph.Node
import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.DijkstraShortestPathSolver
import spock.lang.Specification
import spock.lang.Unroll

class DijkstraShortestPathSolverSpec extends Specification {
    Map<String, Node> nodes
    DijkstraShortestPathSolver shortestPathSolver = new DijkstraShortestPathSolver()

    CloserThanSolver closerThanSolver = Mock(CloserThanSolver)
    Graph graph = new Graph(shortestPathSolver, closerThanSolver)

    def setup() {
        nodes = (1..10).collect { graph.addNode("node_$it") }.collectEntries { [it.name, it] }.asImmutable()
    }

    @Unroll
    def 'shortest path from #from to #to is #expectedWeight'() {
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
        graph.addEdge('node_9', 'node_10', 8)

        when:
        def shortestPath = shortestPathSolver.shortestPath(nodes[from], nodes[to])

        then:
        shortestPath.totalDistance == expectedWeight
        shortestPath.path*.to*.name == expectedPath

        where:
        from     | to        | expectedWeight    | expectedPath
        'node_1' | 'node_1'  | 0                 | []
        'node_1' | 'node_2'  | 1                 | ['node_2']
        'node_1' | 'node_6'  | 10                | ['node_3', 'node_4', 'node_5', 'node_6']
        'node_2' | 'node_8'  | 14                | ['node_3', 'node_4', 'node_5', 'node_8']
        'node_1' | 'node_10' | 11                | ['node_9', 'node_10']
        'node_2' | 'node_10' | Integer.MAX_VALUE | []
    }

    def 'shortest path to self is always 0'() {
        given:
        graph.addEdge('node_1', 'node_1', 1)

        when:
        def shortestPath = shortestPathSolver.shortestPath(nodes['node_1'], nodes['node_1'])

        then:
        shortestPath.totalDistance == 0
        shortestPath.path == []
    }

    def 'shortest path to unreachable node is Integer.MAX_VALUE'() {
        when:
        def shortestPath = shortestPathSolver.shortestPath(nodes['node_1'], nodes['node_2'])

        then:
        shortestPath.totalDistance == Integer.MAX_VALUE
        shortestPath.path == []
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
        def shortestPath = shortestPathSolver.shortestPath(nodes['node_1'], nodes['node_4'])

        then:
        shortestPath.totalDistance == 2
        shortestPath.path*.to*.name == ['node_2', 'node_4']
    }

    def 'complex graph with loop back'() {
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

        when:
        def shortestPath = shortestPathSolver.shortestPath(nodes['node_1'], nodes['node_8'])

        then:
        shortestPath.totalDistance == 14
        shortestPath.path*.to*.name == ['node_2', 'node_4', 'node_3', 'node_6', 'node_8']
    }
}
