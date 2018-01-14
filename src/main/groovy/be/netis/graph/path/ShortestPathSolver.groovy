package be.netis.graph.path

import be.netis.graph.Node

interface ShortestPathSolver {

    /**
     * Finds the shortest path between two nodes.
     * @param from the node to start from
     * @param to the destination node
     * @return
     * the distance between the two nodes. The value is 0 if from and to are the same node, or {@link Integer#MAX_VALUE}
     * if there is no possible path between the two nodes.
     */
    Path shortestPath(Node from, Node to)
}
