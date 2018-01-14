import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{request_tracker}] [%X{user}] [%X{ip}] %level %logger{5} - %msg%n"
        pattern = "%d{HH:mm:ss.SSS} [%thread] - %level - %logger{5} - %msg%n"
    }
}

logger 'be.netis', INFO, ['STDOUT'], false
//logger 'be.netis.graph.path.DijkstraShortestPathSolver', DEBUG, ['STDOUT'], false
//logger 'be.netis.graph.path.BreadthFirstCloserThanSolver', DEBUG, ['STDOUT'], false

root(ERROR, ['STDOUT'])
