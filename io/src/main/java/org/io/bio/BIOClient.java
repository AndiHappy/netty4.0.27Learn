package org.io.bio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BIOClient {

   public static void main(String [] args) {
     for (int i = 0;i< 60000;i++){
    	 try {
       	    String serverName = "127.0.0.1";
            int port = 9090;
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("客户端编码 "+i+ " : "+ client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            //服务端没有响应的时候的阻塞的地址
            String serverString = in.readUTF();
            System.out.println("Server says " + serverString);
            client.close();
         } catch (Exception e) {
            e.printStackTrace();
         } 
     }
      
   }
}