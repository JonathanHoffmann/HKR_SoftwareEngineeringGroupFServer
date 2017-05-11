package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Hossein Eldelbani and Jonathan Hoffmann
 */
public class EchoThread extends Thread {

    protected Socket socket;
    private int userNum = 0;
    private final String path = "";

    public EchoThread(Socket clientSocket, int i) {
        this.socket = clientSocket;
        userNum = i;
    }

    @Override
    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes("cnxok\r\n");//connection istabileshed
            System.out.println("user number " + userNum + " is connected");
        } catch (Exception e) {
            System.out.println("exceprion\n" + e);
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                System.out.println("Command is " + line);
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else // execute(line);
                {
                    if (line.equals("login") || line.equals("signup") || line.equals("logout")) {
                        String email = Methods.sendDataAndReceive(out, brinp, "listening");
                        if (email != null) {
                            if (line.equals("logout")) {
                                String s = Methods.logout(email);
                                Methods.sendData(out, s);
                                System.out.println(s);
                                System.out.println(path);
                            } else {
                                String pass = Methods.sendDataAndReceive(out, brinp, "listening");
                                if (pass != null) {//check in data bases
                                    if (line.equals("login")) {
                                        String s = Methods.CheckEmailAndPass(email, pass);
                                        Methods.sendData(out, s);
                                        System.out.println(s);
                                    } else if (line.equals("signup")) {
                                        String s = Methods.CheckEmail(email, pass);
                                        Methods.sendData(out, s);
                                        System.out.println(s);
                                    }
                                } else {
                                    Methods.sendData(out, "error");
                                }
                            }
                        } else {
                            out.writeBytes("error\r\n");
                            out.flush();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("exeption\n" + e);
                return;
            }
        }
    }
}
