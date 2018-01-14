package be.netis.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@Immutable
@ToString(includes = ['name'], includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = ['name'])
class Node {
    String name
    private List<Edge> edges = Collections.synchronizedList([])

    Node addEdge(Edge edge) {
        edges << edge
        log.debug 'node {} has {} edges', name, edges.size()
        this
    }

    Node removeEdges(Node toNode) {
        def countBefore = edges.size()
        edges.removeIf({ it.to == toNode })
        log.debug 'removed {} edges from node {}', countBefore - edges.size(), name
        this
    }

    List<Edge> getEdges() {
        edges.asImmutable()
    }
}
