package org.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer extends Thread {
	private ServerSocket serverSocket;

	public BIOServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		// 等待客户端连接的超时时间
		serverSocket.setSoTimeout(10000);
	}

	public void run(){
		while (true) {
			System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
			Socket socket = null;
			BufferedReader in = null;
			PrintWriter out = null;
			try {
				// 没有客户端连接，服务端阻塞等待点
				socket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				String currentTime = null;
				String body = null;
				while (true) {
					body = in.readLine();
					if (body == null)
						break;
					System.out.println("The time server receive order : " + body);
					currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
							? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					out.println(currentTime);
				}

			} catch (Exception e) {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (out != null) {
					out.close();
					out = null;
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					socket = null;
				}
			}
		}
	}

	public static void main(String[] args) {
		int port = 9090;
		try {
			Thread t = new BIOServer(port);
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}