package tris.src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastManager {
    // the datagram packet must be sent to address 230.0.0.1 on port 2000
    private final int multicastPort = 2000;
    private final String strMulticastAddress ="230.0.0.1";

    public void sendPort(int port){
        new MulticastSender(port).start();
    }

    class MulticastSender extends Thread {
        private final MulticastSocket mSocket;
        InetAddress multicastAddress;
        DatagramPacket dp;

        public MulticastSender(int serverPort) {
            String strBuf = "" + serverPort;
            byte[] buf = strBuf.getBytes();
            try {
                multicastAddress = InetAddress.getByName(strMulticastAddress);
                mSocket = new MulticastSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dp = new DatagramPacket(buf, buf.length, multicastAddress, multicastPort);
        }

        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                // Sends a multicast datagram containing the server socket port
                try {
                    mSocket.send(dp);
                    //noinspection BusyWait
                    Thread.sleep(20000);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public String[] readAddressAndPort () throws IOException {
        String[] result = new String[2];
        InetAddress group = InetAddress.getByName(strMulticastAddress);
        MulticastSocket mSocket = new MulticastSocket(multicastPort);
        //noinspection deprecation
        mSocket.joinGroup(group);
        byte[] buf = new byte[50];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        mSocket.receive(packet);
        InetAddress remoteAddress = packet.getAddress();
        result[0] = remoteAddress.getHostAddress();
        String received = new String(packet.getData());
        int i = 0;
        while (Character.isDigit(received.charAt(i)))
            i++;
        String receivedPort = received.substring(0, i);
        result[1] = receivedPort;
        mSocket.close();
        return result;
    }

}