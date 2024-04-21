package tris.v2;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TrisServer {
    boolean running = true;
    List<Player> players = Collections.synchronizedList(new LinkedList<Player>());
    AtomicInteger playersCounter = new AtomicInteger(0);

    private final Semaphore mutexPlayers, readyPlayers;

    private synchronized void printInfo (String msg) {
        System.out.println(msg);
    }
    private synchronized void printError (String msg) {
        System.err.println(msg);
    }

    public TrisServer() {
        try {

            mutexPlayers = new Semaphore(1);
            readyPlayers = new Semaphore(0);
            new ClientAccepter().start();
            new GameStarter().start();
            new MulticastManager().sendPort(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class ClientAccepter extends Thread {
        public void run() {
            int serverPort = 8888;
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(serverPort);
                printInfo("Accepting clients");
                while (running) {
                    Socket client = ss.accept();
                    new ClientManager(client).start();
                }
            } catch (IOException e) {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException ex) {
                        printError(ex.toString());
                    }
                }
                printError(e.toString());
            }
        }
    }

    class ClientManager extends Thread {
        Socket client;

        public ClientManager(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject("Welcome to Tris! Insert your username");
                String username = (String) in.readObject();
                int id = playersCounter.addAndGet(1);
                Player p = new Player(id, username, client);
                players.add(p);
                out.writeObject(printAvailablePlayers());


            } catch (IOException e) {
                printError("ClientManager stream error: \n"+e);
            } catch (ClassNotFoundException e) {
                printError("ClientManager input stream error: \n" + e);
            }
        }

        String printAvailablePlayers() {
            StringBuilder sb = new StringBuilder();
            int c = 0;
            for (Player p : players) {
                if(p.isWaiting()){
                    c++;
                    sb.append(p.getID()).append(". ").append(p.getUsername()).append("\n");
                }
            }
            if(c == 0){
                return "There are no available players. Wait for someone to challenge you";
            }
            return "There are " + c + " available players:\n" + sb +
                    "\nType the number of the player you want to challenge or wait for someone to challenge you";
        }
    }

    class GameStarter extends Thread {
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    readyPlayers.acquire(2);
                    mutexPlayers.acquire();
                    Player p1 = players.pop();
                    Player p2 = players.pop();
                    //System.out.println("Starting game: "+p1+" vs "+p2);
                    new GameManager(p1, p2).start();
                    mutexPlayers.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } // while
        } // run
    }

    public static void main(String[] args) {
        new TrisServer();
    }

}