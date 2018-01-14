package be.netis.io

import be.netis.graph.Graph
import be.netis.graph.Node
import be.netis.graph.path.CloserThanSolver
import be.netis.graph.path.ShortestPathSolver
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GraphModelSpec extends Specification {
    Graph graph = new Graph(Mock(ShortestPathSolver), Mock(CloserThanSolver))
    List<Node> nodes

    def setup() {
        nodes = (1..10).collect { graph.addNode("node-$it") }.asImmutable()
    }

    def "map to d3 model"() {
        given:
        graph.addEdge('node-1', 'node-2', 1)
        graph.addEdge('node-1', 'node-3', 4)
        graph.addEdge('node-1', 'node-6', 20)
        graph.addEdge('node-1', 'node-9', 3)
        graph.addEdge('node-2', 'node-4', 9)
        graph.addEdge('node-2', 'node-3', 5)
        graph.addEdge('node-3', 'node-4', 2)
        graph.addEdge('node-3', 'node-5', 11)
        graph.addEdge('node-4', 'node-5', 3)
        graph.addEdge('node-5', 'node-6', 1)
        graph.addEdge('node-5', 'node-8', 4)
        graph.addEdge('node-6', 'node-7', 10)
        graph.addEdge('node-7', 'node-8', 3)
        graph.addEdge('node-9', 'node-10', 8)

        when:
        def model = GraphModel.map(nodes)

        then:
        model.nodes.size() == nodes.size()
        model.nodes*.name as Set == nodes*.name as Set

        and:
        model.edges.size() == 14
        model.edges.each { edge ->
            def source = nodes.find { it.name == model.nodes[edge.source].name }
//            assert source != null

            def target = nodes.find { it.name == model.nodes[edge.target].name }
//            assert target
//
            def e = source.edges.find { it.to.name == target.name }
//            assert e
//            assert e.weight == edge.weight
        }

        and:
        def json = JsonOutput.toJson(model)
        json
    }
}
