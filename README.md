# AkkaStarterKit
This is the Starter Kit for all participants who want to develop their solution based on a Spring-boot Akka architecture.

If you want to use a different technology, this starterkit still provides you with a race track simulator that allows you to verify your network protocol implementation and a provides you with a training opportunity for your algorithm

## prerequisites
  - java jdk 8 installed
  - JAVA_HOME environment variable points to the Java installation directory
  - maven 2 installed
  - git installed
  - rabbitmq installed (for remote connections)

To install the starter kit, do the following:

```shell
  $ git clone https://github.com/FastAndFurious/AkkaStarterKit
  $ cd AkkaStarterKit
  $ mvn clean install
  $ java -jar target/fnf.starterkit-1.0-SNAPSHOT.jar  <options>
```

When building against the snapshots of clientapi and simulib, make sure you build those prior to the above so you have them in your local maven cache. To do just that, do the following

```shell
  $ git clone https://github.com/FastAndFurious/fnf.clientapi
  $ cd fnf.clientapi
  $ mvn clean install
  $ git clone https://github.com/FastAndFurious/fnf.simulib
  $ cd fnf.simulib
  $ mvn clean install
```

 This starts a web application on your computer. Point your browser to http://localhost:8089 to access the simulator.

  **command line options for the executable jar file:** 
- **no options**
  With no options at all, the simulator and the pilot code will both start up and talk in-memory with each other.
  The default strategy for the given pilot is to increase power until she receives the first speed penalty, then       decrease power with every subsequent penalty, until no penalties are caused anymore. Of course, this is not exactly   a winning strategy, but it should provide you an easy starting point for your own development.

- ```--server.port=<port> ```  a port name of your choice, if the default 8089 is used.

- ```-p rabbit```
  As of now, we only support the rabbitmq protocol for connections from the pilot. For that you need to have a         running rabbitmq server installed on your local machine, or somewhere else, in which case you need to supply
  --javapilot.rabbitUrl=<rabbitmq host> as a runtime parameter

- ```-f [ simulator | pilot ] -p rabbit```
  Only when -p rabbit is also provided.
  Starts the executable as "simulator" only, or "pilot" only, resp. Be sure to use --server.port option to use a       different   port for one of the processes in case you start both a pilot and a simulator on the same machine.
  Now you have a standalone simulator that you could also use with any other technology, as long as the pilot knows    how to talk to a rabbit queue
  



```java
```

