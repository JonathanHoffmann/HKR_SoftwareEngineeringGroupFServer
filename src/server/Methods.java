package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hossein Eldelbani and Jonathan Hoffmann
 */
public abstract class Methods
{

   private static String dbURL = "jdbc:mysql://localhost:3306/online_text_editor2?autoReconnect=true&useSSL=false";
   private static String username = "root";
   private static String password = "1234";
   private static Connection conn;

   public static String sendDataAndReceive(DataOutputStream out, BufferedReader brinp, String data)
   {
      String dataReceived = "";
      try
      {
         out.writeBytes(data + "\r\n");
         out.flush();
         dataReceived = brinp.readLine();
      } catch (Exception ex)
      {
         System.out.println("Methods Class....sendDataAnReceive method\n" + ex);
      }
      return dataReceived;
   }

   public static void sendData(DataOutputStream out, String data)
   {
      String dataReceived = "";
      try
      {
         out.writeBytes(data + "\r\n");
         out.flush();
      } catch (Exception ex)
      {
         System.out.println("Methods Class....sendData method\n" + ex);
      }
   }

   public static String receiveData(BufferedReader brinp)
   {
      String dataReceived = "";
      try
      {
         dataReceived = brinp.readLine();
      } catch (Exception ex)
      {
         System.out.println("Methods Class....receiveData method\n" + ex);
      }
      return dataReceived;
   }

   public static boolean connectToDB()
   {

      try
      {
         conn = DriverManager.getConnection(dbURL, username, password);
         System.out.println("DB is connected");
         return true;
      } catch (SQLException ex)
      {
         System.out.println(ex);
         return false;
      }
   }

   public static boolean disConnectDB()
   {

      try
      {
         conn.close();
         System.out.println("DB is disconnected");
         return true;
      } catch (SQLException ex)
      {
         System.out.println(ex);
         return false;
      }
   }

   private static String decrypt(String pass)
   {
      String decrypted = "";
      String[] arr = pass.split("");//make the string into small strings each of one char
      for (int i = 0, j = 1; i < arr.length - 2; i += 3, j++)
      {
         int a = Integer.valueOf(arr[i]) * 100 + Integer.valueOf(arr[i + 1]) * 10 + Integer.valueOf(arr[i + 2]) - (2 * j + 1);
         decrypted += (char) a;
      }
      return decrypted;
   }

   public static ResultSet selectSQL(String sql) throws SQLException
   {
      Statement statement = conn.createStatement();
      ResultSet result = statement.executeQuery(sql);
      return result;
   }

   public static PreparedStatement updateInsert(String sql) throws SQLException
   {
      PreparedStatement statement = conn.prepareStatement(sql);
      return statement;
   }

   public static void changeUpdatedStatus(String email, String path)
   {
      try
      {
         String sql = "UPDATE file_has_participiants SET have_the_last_update=? WHERE path='" + path + "' and email='" + email + "'";
         PreparedStatement ps1 = updateInsert(sql);
         ps1.setString(1, "yes");
         int rowsUpdated = ps1.executeUpdate();
         if (rowsUpdated > 0)
         {
            System.out.println(" who updated it is yes now");
         }
      } catch (SQLException ex)
      {
         System.out.println("exception on changeStatusAfterOpen method\n" + ex);
      }
   }

   public static void changeUpdatedByAndChangeHaveUpdate(String email, String path)
   {
      try
      {
         String sql = "UPDATE file_has_participiants SET have_the_last_update=? WHERE path='" + path + "'";
         String sql2 = "UPDATE file SET updatedby=? , lastupdate=? where path='" + path + "'";
         PreparedStatement ps = updateInsert(sql);
         ps.setString(1, "no");
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated > 0)
         {
            System.out.println("updated to no all");
         }
         changeUpdatedStatus(email, path);
         PreparedStatement ps2 = updateInsert(sql2);
         ps2.setString(1, email);
         ps2.setString(2, "9999-12-31 23:39:39");
         rowsUpdated = ps2.executeUpdate();
         if (rowsUpdated > 0)
         {
            System.out.println(" updated by and update time is registered now");
         }
      } catch (SQLException ex)
      {
         System.out.println("exception changeUpdatedByAndChangeHaveUpdate\n" + ex);
      }
   }

   public static String changeEmailStats(String stat, String email)
   {
      try
      {
         String sql = "UPDATE login_info SET status_now=? WHERE email='" + email + "'";
         PreparedStatement statement = updateInsert(sql);
         statement.setString(1, stat);
         int rowsUpdated = statement.executeUpdate();
         if (rowsUpdated > 0)
         {
            System.out.println("it was dis and now con");
            return stat;
         }
      } catch (SQLException ex)
      {
         System.out.println("changeEmailStats exception " + ex);
         return "error";
      }
      return "error";
   }

   public static String CheckEmailAndPass(String email, String pass)
   {
      if (connectToDB())
      {
         String id = "";
         String sql = "SELECT * FROM login_info";
         ResultSet result;
         try
         {
            result = selectSQL(sql);
            while (result.next())
            {
               if (result.getString("email").equals(email))
               {
                  if (result.getString("pass").equals(pass))
                  {
                     System.out.println("email and password match");
                     System.out.println(email + " is connected");
                     changeEmailStats("con", email);
                     id = result.getString("id");
                     System.out.println("Id = " + id);
                     File dir = new File("./localdisk/" + id);
                     if (!dir.isDirectory())
                     {
                        System.out.println("dir is created");
                        dir.mkdir();
                     } else
                     {
                        System.out.println("dir already exist");
                     }
                     disConnectDB();
                     return "connectedX" + id;
                  }
               }
            }
         } catch (SQLException ex)
         {
            System.out.println("CheckEmailAndPass methods exception\n" + ex);
            return "error";
         }

      }
      return "emailorpasserror";
   }

   public static String logout(String email)
   {
      if (connectToDB())
      {
         changeEmailStats("dis", email);
         disConnectDB();
         return "disconnected";
      }
      return "error";
   }

   public static boolean receiveTextFile(String path, String textName, Socket socket, int length, DataOutputStream out, BufferedReader brinp) throws IOException
   {
      ArrayList<String> list = new ArrayList<>();
      for (int i = 0; i < length + 1; i++)
      {
         String s = receiveData(brinp);
         if (s.equals("endoffile"))
         {
            System.out.println("file received");
            sendData(out, "filereceived");
            Files.write(Paths.get(path + "/" + textName), list, StandardOpenOption.CREATE_NEW);
            break;
         } else
         {
            list.add(s + "\n");
            sendData(out, "continue");
         }
      }
      return list.size() == length;
   }

   public static boolean receiveTextFileUpdate(String path, String access, Socket socket, int length, DataOutputStream out, BufferedReader brinp) throws IOException
   {
      ArrayList<String> list = new ArrayList<>();
      File file = new File(path);
      String backupPath = titleWithoutExtension(path) + "_backup.txt";

      if (file.exists())
      {
         File back = new File(backupPath);
         if (back.exists())
         {
            back.delete();
         }
         Files.copy(Paths.get(path), Paths.get(backupPath), StandardCopyOption.COPY_ATTRIBUTES);
         file.delete();
      }
      for (int i = 0; i < length + 1; i++)
      {
         String s = receiveData(brinp);
         if (s.equals("endoffile"))
         {
            System.out.println("file received");
            Files.write(Paths.get(path), list, StandardOpenOption.CREATE_NEW);
            if (access.equals("ad"))
            {
               String parentPath = file.getParent() + "/administrator backup";
               String fileName = titleWithoutExtension(file.getName());
               File adbackupDIR = new File(parentPath);
               if (!adbackupDIR.exists())
               {
                  adbackupDIR.mkdir();
               }
               String backupAdPath = parentPath + "/" + fileName;
               File adbackupFILE = new File(backupAdPath + "_ad_backup.txt");
               File oldAdBackup = new File(backupAdPath + "_ad_backup_old.txt");
               if (adbackupFILE.exists())
               {
                  if (oldAdBackup.exists())
                  {
                     oldAdBackup.delete();
                  }
                  Files.copy(Paths.get(backupAdPath + "_ad_backup.txt"), Paths.get(backupAdPath + "_ad_backup_old.txt"), StandardCopyOption.COPY_ATTRIBUTES);
                  adbackupFILE.delete();
               }
               Files.write(Paths.get(parentPath + "/" + fileName + "_ad_backup.txt"), list, StandardOpenOption.CREATE_NEW);
            }
            break;
         } else
         {
            list.add(s + "\n");
         }
      }
      return list.size() == length;
   }

   public static String titleWithoutExtension(String title)
   {
      int i = 0;
      for (i = title.length() - 1; i >= 0; i--)
      {
         if (title.charAt(i) == '.')
         {
            break;
         }
      }
      return title.substring(0, i);
   }

   public static List<String> getTheTextFile(String p) throws IOException
   {
      File file = new File(p);
      List<String> list = null;
      if (file.exists())
      {
         list = Files.readAllLines(Paths.get(p));
      }
      return list;
   }

   private static boolean deleteSQL(String sql, String cond) throws SQLException
   {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, cond);
      int rowsDeleted = statement.executeUpdate();
      if (rowsDeleted > 0)
      {
         return true;
      }
      return false;
   }

   public static String haveAccessTo(String email, String p, String request)
   {
      try
      {
         String sql = "select email,path,access,have_the_last_update from file_has_participiants";
         ResultSet result = selectSQL(sql);
         while (result.next())
         {
            System.out.println("loop");
            if (result.getString("path").equals(p))
            {
               System.out.println("path");
               if (result.getString("email").equals(email))
               {
                  System.out.println("email check");
                  if (request.equals("push"))
                  {
                     return result.getString("access") + "X" + result.getString("have_the_last_update");
                  } else
                  {
                     return "yes";
                  }
               }
            }
         }
      } catch (SQLException ex)
      {
         System.out.println("haveAccessTo method exception\n" + ex);
      }
      return "no";
   }

   public static String deleteThisPath(String p, String email)
   {
      try
      {
         boolean pathExist = false;
         String sql = "select path,createdby from file ";
         ResultSet result = selectSQL(sql);
         while (result.next())
         {
            if (result.getString("path").equals(p))
            {
               pathExist = true;
               if (result.getString("createdby").equals(email))
               {
                  //delete
                  sql = "DELETE FROM file WHERE path=?";
                  String sql2 = "DELETE FROM file_has_participiants WHERE path=?";
                  if (deleteSQL(sql, p) && deleteSQL(sql2, p))
                  {
                     File file = new File(p);
                     if (file.exists())
                     {
                        file.delete();
                        return "deleted";
                     }
                  }
               } else
               {
                  return "error";
               }
            }
         }
         if (!pathExist)
         {
            return "error";
         }
      } catch (SQLException ex)
      {
         System.out.println("deleteThisPath method exception\n" + ex);
      }
      return "error";
   }

   public static ArrayList<String> getFilesFor(String email)
   {//return the paths of the files of the email with the creator of each file and the access
      try
      {
         ArrayList<String> arr = new ArrayList<>();
         String sql = "select path,access,have_the_last_update from file_has_participiants where email='" + email + "'";
         ResultSet result = selectSQL(sql);
         while (result.next())
         {
            String creator = "";
            String updatedby = "";
            sql = "select createdby,updatedby from file where path='" + result.getString("path") + "'";
            ResultSet res = selectSQL(sql);
            while (res.next())
            {
               creator = res.getString("createdby");
               updatedby = res.getString("updatedby");
               System.out.println("created by  " + creator + " updated by" + updatedby);
            }
            arr.add(result.getString("path") + "X" + result.getString("access") + "X" + result.getString("have_the_last_update") + "X" + creator + "X" + updatedby);
            creator = "error reading creator";
         }
         return arr;
      } catch (SQLException ex)
      {
         Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }

   public static boolean insertFile(String fileName, String path, String email, String createdTime, String updatedBy, String lastUpdate)
   {
      try
      {
         String sql = "INSERT INTO file(name,path,createdby,createtime,updatedby,lastupdate) VALUES (?,?,?, ?,?,?)";
         PreparedStatement Pstatement = updateInsert(sql);
         Pstatement.setString(1, fileName);
         Pstatement.setString(2, path);
         Pstatement.setString(3, email);
         Pstatement.setString(4, createdTime);
         Pstatement.setString(5, updatedBy);
         Pstatement.setString(6, lastUpdate);
         int rowsInserted = Pstatement.executeUpdate();
         if (rowsInserted > 0)
         {
            System.out.println("A new FILE was inserted successfully!");
            return true;
         }
      } catch (SQLException ex)
      {
         Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);

      }

      return false;
   }

   public static boolean insertParticipiant(String email, String path, String access, String haveTheLastUpdate)
   {

      try
      {
         String sql = "INSERT INTO file_has_participiants(email,path,access,have_the_last_update) VALUES (?,?,?,?)";
         PreparedStatement Pstatement = updateInsert(sql);
         Pstatement.setString(1, email);
         Pstatement.setString(2, path);
         Pstatement.setString(3, access);
         Pstatement.setString(4, haveTheLastUpdate);
         int rowsInserted = Pstatement.executeUpdate();
         if (rowsInserted > 0)
         {
            System.out.println("A new PARTICIPIANT was inserted successfully!");
            return true;
         }
      } catch (SQLException ex)
      {
         Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);

      }
      return false;
   }

   public static String CheckEmail(String email, String pass) throws SQLException
   {
      if (connectToDB())
      {

         String sql = "SELECT * FROM login_info";
         ResultSet result = selectSQL(sql);
         boolean emailExist = false;
         while (result.next())
         {
            if (result.getString("email").equals(email))
            {
               emailExist = true;
               break;
            }
         }
         if (!emailExist)
         {
            sql = "INSERT INTO login_info (email,pass) VALUES (?, ?)";
            PreparedStatement Pstatement = updateInsert(sql);
            Pstatement.setString(1, email);
            Pstatement.setString(2, pass);
            int rowsInserted = Pstatement.executeUpdate();
            if (rowsInserted > 0)
            {
               System.out.println("A new user was inserted successfully!");
            }
            sql = "SELECT * FROM login_info";
            result = selectSQL(sql);
            while (result.next())
            {
               if (result.getString("email").equals(email))
               {
                  String id = result.getString("id");
                  System.out.println("Id = " + id);
                  File dir = new File("./localdisk/" + id);
                  if (!dir.isDirectory())
                  {
                     System.out.println("dir is created");
                     dir.mkdir();
                  }
               }
            }
            disConnectDB();
            return "created";
         } else
         {
            System.out.println("email exist");
            disConnectDB();
            return "emailexist";
         }
      }
      return "error";
   }
}