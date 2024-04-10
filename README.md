# Tris
This is a <b> Tik Tak Toe </b> network game.
## Project structure
The application is written in java and works with a server and multiple clients:
the server accepts the clients and hosts the games, 
while each client represents a player waiting to start a new game.
Client and server use a TCP socket connection. <br>
Every time a new client connects to the server, the client is added to the list of waiting players.
When there are at least two players waiting in the list, the server starts a new game between them.
All the clients and all the components of the server are executed by different threads.<br>