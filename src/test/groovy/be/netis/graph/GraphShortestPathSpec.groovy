package be.netis.graph

import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.Path
import be.netis.graph.path.ShortestPathSolver
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GraphShortestPathSpec extends Specification {

    ShortestPathSolver shortestPathSolver = Mock(ShortestPathSolver)
    CloserThanSolver closerThanSolver = Mock(CloserThanSolver)
    Graph graph = new Graph(shortestPathSolver, closerThanSolver)

    def "graph delegates shortest path queries to solver"() {
        given: 'some nodes'
        def from = graph.addNode('from')
        def to = graph.addNode('to')

        when:
        def weight = graph.shortestPath('from', 'to')

        then:
        weight == 100

        and:
        1 * shortestPathSolver.shortestPath(from, to) >> new Path(100)
    }

    def "shortest from unknown node throws an exception"() {
        given: 'some nodes'
        graph.addNode('to')

        when:
        graph.shortestPath('unknown', 'to')

        then:
        def e = thrown(NodeNotFoundException)
        e.name == 'unknown'
    }

    def "shortest to unknown node throws an exception"() {
        given: 'some nodes'
        graph.addNode('from')

        when:
        graph.shortestPath('from', 'unknown')

        then:
        def e = thrown(NodeNotFoundException)
        e.name == 'unknown'
    }
}
