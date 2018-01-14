package be.netis.graph

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includeNames = true, includePackage = false)
class Edge {
    final Node to
    final int weight
}
