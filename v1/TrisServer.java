package tris.v1;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class TrisServer {
    private final ServerSocket ss;
    private final LinkedList<Player> waitingPlayers;
    private int numPlayers;
    private final Semaphore mutexPlayers, readyPlayers;

    public TrisServer() {
        try {
            int serverPort = 8888;
            ss = new ServerSocket(serverPort);
            waitingPlayers = new LinkedList<>();
            numPlayers=0;
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
        public void run(){
            System.out.println("Accepting clients");
            //noinspection InfiniteLoopStatement
            while(true){
                try {
                    Socket c = ss.accept();
                    mutexPlayers.acquire();
                    numPlayers++;
                    Player p = new Player(c, "Player "+numPlayers);
                    if(!waitingPlayers.contains(p)){
                        waitingPlayers.add(p);
                        System.out.println("Accepted "+p+" {"+p.getSock().getInetAddress()+"}");
                    } else {
                        numPlayers--;
                        System.out.println("Can't accept "+p+" {"+p.getSock().getInetAddress()+"}");
                    }
                    readyPlayers.release();
                    mutexPlayers.release();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } // while
        } // run
    }

    class GameStarter extends Thread {
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    readyPlayers.acquire(2);
                    mutexPlayers.acquire();
                    Player p1 = waitingPlayers.pop();
                    Player p2 = waitingPlayers.pop();
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