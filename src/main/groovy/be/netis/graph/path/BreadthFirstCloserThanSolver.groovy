package be.netis.graph.path

import be.netis.graph.Edge
import be.netis.graph.Node
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.LocalDateTime

/**
 * Finds the closest nodes using a Breadth first search.
 *
 * {@see https://en.wikipedia.org/wiki/Breadth-first_search}
 */
@Slf4j
@Component
class BreadthFirstCloserThanSolver implements CloserThanSolver {

    @Override
    List<Node> closerThan(int distance, Node from) {
        log.info 'searching for nodes closer than {} from {}', distance, from

        def startDateTime = LocalDateTime.now()
        def result = (distance < 1) ? [] : solve(distance, from).toSorted { a, b -> a?.name <=> b?.name }

        def duration = Duration.between(startDateTime, LocalDateTime.now())
        log.info 'found {} nodes closer than {} from {} in {}ms', result.size(), distance, from, duration.toMillis()
        if (log.isDebugEnabled()) {
            log.debug '{}', result*.name.join(', ')
        }
        return result
    }

    private Set<Node> solve(int maxDistance, Node from) {
        Map<String, EvaluatedNode> result = [:]
        Queue<EvaluatedNode> queue = new PriorityQueue<>(EvaluatedNode.comparator)
        queue.add(new EvaluatedNode(from, 0))

        while (!queue.empty) {
            log.debug 'queue size {}', queue.size()
            def current = queue.remove()

            log.debug 'evaluating {} neighbors', current
            current.node.edges.each { Edge edge ->
                if (edge.to == from) {
                    log.debug 'circled back to starting node {}, skipping', edge.to
                    return
                }

                long distance = current.distance + edge.weight
                log.debug '{} distance {}', edge.to, distance

                def knownResult = result.get(edge.to.name)
                if (knownResult && knownResult.distance < distance) {
                    log.debug 'result already contains node {} at lower distance ({} vs {}), skipping',
                            edge.to, knownResult.distance, distance
                    return
                }

                if (distance < maxDistance) {
                    def evaluatedNode = new EvaluatedNode(edge.to, distance)
                    log.debug 'adding {} to result set', evaluatedNode
                    result.put(edge.to.name, evaluatedNode)

                    // add to queue to evaluate neighbors
                    queue.add(evaluatedNode)
                }
            }
        }

        return result.values()*.node
    }

    private class EvaluatedNode {
        Node node
        long distance

        EvaluatedNode(Node node, long distance) {
            this.node = node
            this.distance = distance
        }

        @Override
        String toString() {
            "EvaluatedNode(node:${node.name}, distance:$distance})"
        }

        static Comparator<EvaluatedNode> getComparator() {
            { a, b -> a.distance <=> b.distance } as Comparator<EvaluatedNode>
        }
    }
}
