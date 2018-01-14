package be.netis.io

import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.Instant

@Slf4j
abstract class Session {

    final String id = UUID.randomUUID().toString()
    final Instant sessionStartedTimestamp = Instant.now()

    String name = 'Anonymous'

    Duration getSessionDuration() {
        return Duration.between(sessionStartedTimestamp, Instant.now())
    }

    void sendGreeting() {
        send "HI, I'M $id"
    }

    void close() {
        def duration = getSessionDuration().toMillis()
        send "BYE $name, WE SPOKE FOR ${duration} MS"
        log.info 'closing session [{}] after {}ms', id, duration
        getDelegate().close()
    }

    abstract void send(String text)

    abstract Closeable getDelegate()
}