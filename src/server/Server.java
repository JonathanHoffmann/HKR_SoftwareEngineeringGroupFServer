package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Hossein Eldelbani and Jonathan Hoffmann
 */
public class Server {

    static final int PORT = 7500;

    //test 1 jonathan
    public static void main(String args[]) {
        
        ServerSocket serverSocket = null;
        Socket socket = null;
        int userNumber = 1;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                
            }
            new EchoThread(socket, userNumber++).start(); // new thread for a client
        }
    }

}
