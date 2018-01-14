# Code challenge

The goal of this code challenge is to create a simple TCP socket server that listens for commands on port 50000. Clients
will connect and create some nodes and edges, then request shortest path or the list of nodes within a given distance
radius.

The project is written using the Groovy language and has only dependencies to SLF4J and Logback.

## Build

A Gradle build file is available within the project.

Run the test using:

``` 
$ gradle check

BUILD SUCCESSFUL in 8s
4 actionable tasks: 4 executed
```

Build Spring boot fat jar, including all dependencies:

```
$ gradle assemble
Starting a Gradle Daemon (subsequent builds will be faster)
:compileJava NO-SOURCE
:compileGroovy
:processResources
:classes
:jar
:findMainClass
:startScripts
:distTar
:distZip
:bootRepackage
:assemble

BUILD SUCCESSFUL

Total time: 8.37 secs

```

The distribution is created in `./build/libs/graph-explorer-0.1.0.jar`.

## Run

### Dist

To start the server using the fat jar, simply run:

```
$ java -jar graph-explorer-0.1.0.jar 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.7.RELEASE)

20:35:52.596 [main] - INFO - b.n.Application - Starting Application on mac-dng with PID 10392
20:35:52.598 [main] - INFO - b.n.Application - No active profile set, falling back to default profiles: default
20:35:54.116 [cli-socket] - INFO - b.n.CliServer - starting socket server on port 50000
20:35:54.119 [cli-socket] - INFO - b.n.CliServer - waiting for new connection
20:35:54.977 [main] - INFO - b.n.Application - Started Application in 2.708 seconds (JVM running for 3.748)
```

### Sources

The project comes with a gradle build file that can be used to generate the package or start the server.

```
$ ./gradlew bootrun

> Task :bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.7.RELEASE)

20:33:59.297 [main] - INFO - b.n.Application - Starting Application on mac-dng with PID 9987 
20:33:59.299 [main] - INFO - b.n.Application - No active profile set, falling back to default profiles: default
20:34:00.623 [cli-socket] - INFO - b.n.CliServer - starting socket server on port 50000
20:34:00.627 [cli-socket] - INFO - b.n.CliServer - waiting for new connection
20:34:01.441 [main] - INFO - b.n.Application - Started Application in 2.368 seconds (JVM running for 3.302)
<==========---> 83% EXECUTING [16s]

```

## Code structure

The project is a Spring Boot web app using the class `be.netis.Application` as entry point. On application startup the
`be.netis.CliServer` bean is created and starts a TCP socket listening on port 50000.

The application also exposes a Web socket server listening on port 8080, and reachable at `ws://localhost:8080/ws`.
The `be.netis.WebSocketConfiguration` instantiate a `be.netis.io.WebSocketHandler` that handle the client connection
and messages. 

For the graph, the main class is `be.netis.graph.Graph`, that will contains all nodes. This class delegates shortest
path and close neighbors search to (autowired) concrete implementations of `be.netis.graph.path.ShortestPathSolver` and 
`be.netis.graph.path.CloserThanSolver`.

## Clients

### CLI

Using a telnet connection it's possible to send commands directly to the server.

### Web

A web client is available in `src/main/resources/static/index.html` and reachable at `http://localhost:8080`. The 
interface is pretty straight forward: once connected, use the first input text to send commands to the server. Command
and server output are visible in the "Console" panel.

The map panel is used to visualize the graph thanks to the D3 javascript library. To render the graph from a given node
perspective, simply type the node name followed by the maximum distance and click the "Energize" button.

Some graph events are broadcast to all web clients, and might trigger a graph refresh (add/remove edges).  

## Commands

Commands are case insensitive, but the arguments are case sensitive. Server always shouts when replying.

### hi, I'm <name>

Sets the session name, e.g. `hi, I'm John`. The server reply with a `HI John`.

### bye

Close the connection.

### add node <name>

Adds a new node to the graph, e.g. `add node node-1`. The server replies with a `NODE ADDED` or an error message if the
node already exists.

### remove node <name>

Removes a node from the graph, e.g. `remove node node-1`. All edges linked to that node are also removed with this 
operation. The server replies with a `NODE REMOVED` or an error message if the node was not found.

### add edge <from> <to> <weight>

Adds an edge between two nodes with a weight. The server replies with a `EDGE ADDED` or an error message if the source 
or target nodes were not found. There can be multiple edges with different weight between two nodes.

### remove edge <from> <to>

Removes all edges between two nodes, e.g. `remove edge node-1 node-2`. The server replies with a `EDGE REMOVED` or an 
error message if the source or target nodes were not found.

### shortest path <from> <to>

Calculates the length of the shortest path between two nodes, e.g. `shortest path node-1 node-100`. The server replies
with the value or an error message if the source or target nodes were not found.

### closer than <dist> <from>

Lists the nodes closer than a given distance from a node, e.g. `closer than 15 node-1`. The server replies with a comma
separated list of node names, sorted alphabetically. The source node and nodes at the exact distance are not included
in the list. The response is an error message if the source node was not found.

### refresh graph <from> <dist>

Retrieves a json representation of the sub graph starting from a node and within a given distance, e.g. 
`refresh graph node-1 16`. This is what's used behind the "Energize" button to refresh the visualisation of the graph.
