package be.netis.graph.path

import be.netis.graph.Edge
import be.netis.graph.Node
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.LocalDateTime

/**
 * Solves the shortest path between two nodes using the Dijkstra algorithm.
 * {@see https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm}
 */
@Slf4j
@Component
class DijkstraShortestPathSolver implements ShortestPathSolver {

    @Override
    Path shortestPath(Node from, Node to) {
        log.info 'calculating shortest path between nodes [{}, {}]', from, to
        def startDateTime = LocalDateTime.now()
        def shortestPath = (from == to) ? new Path(0) : solve(from, to)

        def duration = Duration.between(startDateTime, LocalDateTime.now())
        log.info 'computed shortest path from {} to {} in {}ms: {}', from, to, duration.toMillis(), shortestPath
        if (log.traceEnabled) {
            log.trace "path: [{}] {}",
                    from.name, shortestPath.path.collect { "--(${it.weight})--> [${it.to.name}]" }.join(' ')
        }
        return shortestPath
    }

    private Path solve(Node from, Node to) {
        Map<String, EvaluatedNode> evaluatedNodes = [:]
        PriorityQueue<EvaluatedNode> queue = new PriorityQueue<>(EvaluatedNode.comparator)

        queue.add(new EvaluatedNode(from, new Path(0L)))

        while (!queue.empty) {
            log.debug 'queue size {}', queue.size()
            EvaluatedNode evaluationNode = queue.remove()

            if (evaluationNode.node == to) {
                log.debug 'reached destination {}', evaluationNode
                return evaluationNode.path
            }

            log.debug 'evaluating node {}', evaluationNode
            evaluationNode.node.edges.each { Edge edge ->
                log.debug 'edge from {} to {} with weight {}', evaluationNode.node, edge.to, edge.weight
                long distance = evaluationNode.path.totalDistance + edge.weight

                if (distance > Integer.MAX_VALUE) {
                    log.debug 'maximum allowed distance reached, discarding path'
                    return
                }

                def knownDistance = evaluatedNodes.get(edge.to.name)
                if (!knownDistance || knownDistance.path.totalDistance > distance) {
                    def evaluatedNode = new EvaluatedNode(edge.to,
                            new Path(distance, evaluationNode.path.path + [edge]))
                    evaluatedNodes.put(edge.to.name, evaluatedNode)
                    queue.add(evaluatedNode)
                } else {
                    log.debug 'there \'s already a closer path to {} ({} vs {})', edge.to, knownDistance, distance
                }
            }
        }

        log.debug '{} is unreachable from {}', to, from
        return new Path(Integer.MAX_VALUE)
    }

    private class EvaluatedNode {
        final Node node
        final Path path

        EvaluatedNode(Node node, Path path) {
            this.node = node
            this.path = path
        }

        @Override
        String toString() {
            "EvaluatedNode(node:${node.name}, path:$path})"
        }

        static Comparator<EvaluatedNode> getComparator() {
            { a, b -> a.path <=> b.path } as Comparator<EvaluatedNode>
        }
    }
}

