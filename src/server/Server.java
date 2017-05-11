/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import javax.swing.JOptionPane;

/**
 *
 * @author hosse
 */
public class Server {

    static final int PORT = 7500;

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
