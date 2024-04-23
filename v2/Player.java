package tris.v2;

import java.net.Socket;

public class Player {
    private final int ID;
    private final String username;
    private final Socket client;
    private boolean available = true;
    private boolean waiting = false;
    private Player opponent = null;

    public Player(int ID, String username, Socket sock) {
        this.ID = ID;
        this.client = sock;
        this.username = username;
    }

    public int getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public Socket getClient() {
        return client;
    }

    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) { this.available = available; }

    public boolean isWaiting() { return waiting; }

    public void setWaiting(boolean waiting) { this.waiting = waiting; }

    public Player getOpponent() { return opponent; }

    public void setOpponent(Player opponent) { this.opponent = opponent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return player.getClient().getInetAddress().equals(client.getInetAddress());
    }

    @Override
    public String toString() { return ID+" - "+username; }
}