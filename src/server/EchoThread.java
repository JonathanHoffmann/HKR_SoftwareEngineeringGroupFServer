package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
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
    private String path = "";

    public EchoThread(Socket clientSocket, int i) {
        this.socket = clientSocket;
        userNum = i;
    }

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
                    } else if (line.equals("save")) {
                        String stat = Methods.sendDataAndReceive(out, brinp, "listening");//waiting login
                        if (stat.equals("login")) {
                            String email = Methods.sendDataAndReceive(out, brinp, "listening");
                            if (email != null) {
                                String pass = Methods.sendDataAndReceive(out, brinp, "listening");
                                if (pass != null) {
                                    String s = Methods.CheckEmailAndPass(email, pass);
                                    Methods.sendData(out, s);//send connected or no
                                    System.out.println(s);
                                    if (s.contains("connected")) {
                                        String[] splitedS = s.split("X");
                                        path = "./localdisk/" + splitedS[1];
                                        stat = Methods.receiveData(brinp);//textName at 0, length at 1
                                        String[] statSplited = stat.split("X");//textName at 0, length at 1
                                        System.out.println("lenght is " + statSplited[1] + " lines");
                                        String realPath = path + "/" + statSplited[0];
                                        File file = new File(realPath);
                                        if (file.exists()) {//request a rename
                                            System.out.println("the file is already exist on server");
                                            Methods.sendData(out, "fileexist");
                                            //do nothing will go and wait new command
                                        } else {
                                            stat = Methods.sendDataAndReceive(out, brinp, "listening");
                                            if (stat.equals("startupload")) {
                                                Methods.sendData(out, "continue");
                                                if (Methods.receiveTextFile(path, statSplited[0], socket, Integer.parseInt(statSplited[1]), out, brinp)) {
                                                    //check the date
                                                    if (Methods.connectToDB()) {
                                                        Methods.insertFile(statSplited[0], realPath, email, "9999-12-31 23:59:59", email, "9999-12-31 23:59:59");
                                                        Methods.insertParticipiant(email, realPath, "ad", "yes");
                                                        Methods.disConnectDB();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    //download
                                } else {
                                    out.writeBytes("error\r\n");
                                    out.flush();
                                }
                            }

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