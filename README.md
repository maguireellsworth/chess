# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Web API Sequence Diagram
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIei0azF5vDgHYsgwr5kvDrco67F8H5LCBALnAWspqig5QIAePKwvuh6ouisTYgmhgumGbpkhSBq0uWo4mkS4YWhyMDcryBqCsKMCvmIrrSkml6waUtHaI6zoErhLLlJ63pBgGQYhsx5qRhy0YwLGwZcdxibJlBlwoTy2a5pg-4gjBJRXAMxGjCBNZBrOzb-O22mFNkPYwP2g69PpI6GZ8xl1o2ZmLmYnCrt4fiBF4KDoHuB6+Mwx7pJkmDWReRTUNe0gAKK7gl9QJc0LQPqoT7dNOploNMFlslppT9Ll7nzuZWlslhpQIfYoXISFvpoRimFythvFkXhHAoNwmSySJblzqRTJuhRpSRMMEA0DJolcVhnbFShoXqQgebKZZsW6T035Xpt0VgKUdlDku3meL5G6Qrau7QjAADio6suFp5ReezA6ded0pel9ijjlJnlYVSlApcpUA3OEGaRt1XtbV0IPaMqjIdCLUYQpHUyHxpLkmAsmMoNM7lSNZoRoUlrUTGc3yEKYRlXOoZdSxOngnj83tRqWPujAOMI2osLE+RBSWqjzDAMq3OPQzo1M2xTryjdsQwBAABm92jvG7OsWcINlArYCretOudj+JVTL9iPjJU-TmygACS0iWwAjL2ADMAAsTwnpkBoVhMXw6AgoANj7wF+08NsAHKjn7ewwI0u1xftb1HQOQ49Gbj2WxU1ujvbTuux7Uxe-qBn3H0-uByAwelysXyR9H5ex-HXkrud66BNgPhQNg3DwLqmRq6MKQRWeOTvbL161A0P1-cE4PoEO9ejAnxzA6mJV9HTHmTn0S9l22VXM-KgmZLzsJwP3KCoxSbWwRzjPYxSrPyATeUC2NklcjylNxtoNMwFvNAUsSbG1hs-YA6N77S0fmAM+e934sTJhyCmEsh70T3sAwWR84KKmVJAnCD93QnxQHA0cUdRgIIkkgq0NpUFiAWlrYqF8vSZANlDI2WsTalgzqMPO5RnbuxgCvDsWsDop3sunXeucHb8ILkIluPl24BEsL1BCyQYAACkIA8kHoYAIAcg6vTHjDRO5RqiUjvC0G2-0hoL16D3YAKioBwAgAhKAswbb22Eb+bW68wa2IqtWOYgcnEuLcUZHhdsZGeUPrLcEAArbRaAz5aLUigNErV8GdWge6HG4DX5E0wR-ahKDwH-0AUUmWW0WZUwgQw7JJNSg81HLCTx0hKGk3ZKUO6FJdH-zaZUiS2DunqzZnfAhOTSiJJ5GfBxoTXHQFmDcchKAOnjUpNgLQA9eYa3GWvYEpRUloGzNgRIBh2GplAaYnaQNThiOOr0U6rc1x+QCF4RxXYvSwGANgHuhB4iJGHi9A6JjdKVESslVK6VjBAz-BtEqkEOFxOPtwPA59UVZnSehLEWTMaEI9BixkegDD80GZ0y0iAvktGzL0ol+h6Ga32ZcSleATlnLEIfD6ZQbk-k7Pc1OjyFFAA
