package be.netis.graph.path

import be.netis.graph.Node

interface CloserThanSolver {

    /**
     * Finds all nodes within a maximum distance.
     * @param distance
     * the maximum distance, nodes with a distance greater or equal to this parameter will be discarded
     * @param from the start node
     * @return a list of distinct nodes within the distance radius, sorted on by {@link Node#name}
     */
    List<Node> closerThan(int distance, Node from)

}