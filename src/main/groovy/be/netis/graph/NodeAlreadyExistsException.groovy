package be.netis.graph

class NodeAlreadyExistsException extends Exception {

    static void throwIf(Boolean condition) {
        if (condition) {
            throw new NodeAlreadyExistsException()
        }
    }
}
