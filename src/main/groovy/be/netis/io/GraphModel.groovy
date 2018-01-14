package be.netis.io

import be.netis.graph.Edge
import be.netis.graph.Node
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class GraphModel {
    List<GraphModelNode> nodes
    List<GraphModelEdge> edges

    static GraphModel map(List<Node> src) {
        def model = new GraphModel()

        model.nodes = src.collect { node -> new GraphModelNode(name: node.name, numberOfEdges: node.edges.size()) }
        Map<String, Integer> nodeIndexTable = model.nodes.collectEntries { [(it.name): model.nodes.indexOf(it)] }

        model.edges = src.collect { Node node ->
            node.edges.collect { Edge edge ->
                new GraphModelEdge(
                        source: nodeIndexTable[node.name],
                        target: nodeIndexTable[edge.to.name],
                        weight: edge.weight)
            }
            .findAll { it.target != null }
        }.flatten() as List<GraphModelEdge>

        return model
    }
}

@ToString
@EqualsAndHashCode(includes = ['name'])
class GraphModelNode {
    String name
    Integer numberOfEdges
}

@ToString
@EqualsAndHashCode
class GraphModelEdge {
    Integer source
    Integer target
    Integer weight
}
