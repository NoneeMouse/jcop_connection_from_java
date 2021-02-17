/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtttool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Sarabjeet Singh
 */
public class SocketConnection {
//initialize socket and input stream 

    private static Socket socket = null;
    private static ServerSocket server = null;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;

    private static SocketConnection socketConnection;

    private SocketConnection() {
    }

    public static SocketConnection getInstance() {
        if (socketConnection == null) {
            socketConnection = new SocketConnection();
        }
        return socketConnection;
    }

    public void clientSocketConnection() throws IOException {
        try {
            JcopCommunicator.getInstance().setCustomOutputText(2);
            server = new ServerSocket(5000);
            System.out.println("Waiting for a client ...");
            JcopCommunicator.getInstance().printLog("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");
            JcopCommunicator.getInstance().printLog("Client accepted");
            // takes input from the client socket 
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            // sends output to the socket 
            out = new DataOutputStream(socket.getOutputStream());
            JcopCommunicator.getInstance().setCustomOutputText(3);
            String line = "";
            // reads message from client until "Over" is sent 
            while (!line.equals("Over")) {
                try {
                    line = in.readUTF();
                    System.out.println(line);
                  //  JcopCommunicator.getInstance().printLog("IN:"+line);
                    String status = JcopCommunicator.getInstance().performCommand(line);
                    out.writeUTF(status);
                    //break;
                } catch (Exception i) {
                    System.out.println(i);
                }
            }

            System.out.println("Closing connection");

            // close connection 
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
