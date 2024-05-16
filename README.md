# Tris
This is a <b> Tic Tac Toe </b> network game.
## How to play
1. Start the TrisServer on a computer connected to your local network.
2. Start the TrisClients on computers connected to the same network of the server.
3. The Server and the Clients will automatically connect to each other.
4. If you start at least 2 clients the server will automatically start a new game between the 2 players.
## Project structure
The application server hosts all the games, managing them with multiple threads to avoid blocking.
While the server is online it shares its address and port through a multicast socket, 
allowing the hosts to connect to him. <br>
The server accepts the clients and hosts the games using multiple threads to avoid unnecessary blocking.
Each client represents a player waiting to start a new game.
The client connects to the server through a TCP socket connection. <br>
Every time a new client connects to the server, the client is added to the list of waiting players.
When there are at least two players waiting in the list, the server starts a new game between them.
All the clients and all the components of the server are executed by different threads.<br>
