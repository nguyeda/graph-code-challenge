package be.netis.graph.path

import be.netis.graph.Edge
import groovy.transform.Sortable

@Sortable(includes = ['totalDistance'])
class Path {
    final List<Edge> path
    final long totalDistance

    Path(long totalDistance, List<Edge> path = []) {
        this.totalDistance = totalDistance
        this.path = (path ?: []).asImmutable()
    }


    @Override
    String toString() {
        return "Path(totalDistance:$totalDistance, hops:${path.size()})"
    }
}
