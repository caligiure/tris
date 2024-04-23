package tris.v2;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TrisServer {
    private final int SERVER_PORT = 8888;
    boolean running = true;
    List<Player> players = Collections.synchronizedList(new LinkedList<Player>());
    AtomicInteger playersCounter = new AtomicInteger(0);

    private synchronized void printInfo (String msg) {
        System.out.println(msg);
    }
    private synchronized void printError (String msg) {
        System.err.println(msg);
    }

    public TrisServer() {
        new ClientAccepter().start();
        new GameStarter().start();
        new MulticastManager().sendPort(SERVER_PORT);
    }

    class ClientAccepter extends Thread {
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(SERVER_PORT);
                printInfo("Accepting clients");
                while (running) {
                    Socket client = ss.accept();
                    new ClientManager(client).start();
                }
            } catch (IOException e) {
                if (ss != null) { try { ss.close(); } catch (IOException ex) { printError(ex.toString()); } }
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
                printInfo("Accepted player "+p);
                out.writeObject(printAvailablePlayers()+"\nType the number of a player to play against him or type R to reload the players list");
                client.setSoTimeout(5000);
                while (p.isAvailable()) {
                    String s = (String) in.readObject();
                    if (s.equalsIgnoreCase("R")) {
                        out.writeObject(printAvailablePlayers());
                    } else {
                        int selectedID = -1;
                        try { selectedID = Integer.parseInt(s); } catch (NumberFormatException e) {
                            try { out.writeObject("Illegal input. Try again"); } catch (IOException ex) { printError(ex.toString()); } }
                        if (selectedID != -1) {
                            Player otherPlayer = findAvailablePlayer(selectedID);
                            if(otherPlayer == null) { out.writeObject("The selected player is not available. Try again"); }
                            else {
                                sendChallenge(p, otherPlayer);
                            }
                        }
                    }
                }
                in.close();
                out.close();
            } catch (IOException e) { printError("ClientManager stream error with client"+client.getInetAddress()+"\n"+e);
            } catch (ClassNotFoundException e) { printError("ClientManager input stream error with client"+client.getInetAddress()+"\n"+e); }
        }

        private void sendChallenge(Player p, Player otherPlayer) {
        }

        String printAvailablePlayers() {
            StringBuilder sb = new StringBuilder();
            int c = 0;
            for (Player p : players) {
                if(p.isAvailable()){
                    c++;
                    sb.append(p).append("Is waiting for an opponent").append("\n");
                }
            }
            if(c == 0){ return "There are no available players"; }
            return "There are " + c + " available players\n" + sb;
        }

        Player findAvailablePlayer(int id) {
            for (Player p : players){
                if (p.getID()==id && p.isAvailable()) {
                    return p;
                }
            }
            return null;
        }
    }

    class GameStarter extends Thread {
        public void run() {
            /*
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

             */
        } // run
    }

    public static void main(String[] args) {
        new TrisServer();
    }

}