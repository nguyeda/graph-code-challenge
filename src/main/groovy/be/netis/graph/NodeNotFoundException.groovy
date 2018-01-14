package be.netis.graph

import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
class NodeNotFoundException extends Exception {

    String name

    static void throwIf(Boolean condition, String name) {
        if (condition) {
            throw new NodeNotFoundException(name: name)
        }
    }
}
