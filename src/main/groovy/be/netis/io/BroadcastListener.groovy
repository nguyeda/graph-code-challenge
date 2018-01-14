package be.netis.io

interface BroadcastListener {

    void sendAll(String message, List<Session> exclude)
}
