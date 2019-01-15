package org.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

/**
 * NioServer 的示例：
 * 
 */
public class NioServer {

	private Selector selector;
	private ServerSocketChannel servChannel;
	private volatile boolean stop = false;

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		NioServer nioServer = new NioServer();
		nioServer.startServer(port);

	}

	private void startServer(int port) throws Exception {
		selector = SelectorProvider.provider().openSelector();
		servChannel = ServerSocketChannel.open();
		servChannel.configureBlocking(false);
		servChannel.socket().bind(new InetSocketAddress(port), 1024);
//		SelectionKey  key = servChannel.register(selector, SelectionKey.OP_ACCEPT);
		SelectionKey  key = servChannel.register(selector, 0,new Object());
		System.out.println("The time server is start in port : " + port);

		while (!stop) {
			try {
				//key值的感兴趣的事件的类型
				key.interestOps(SelectionKey.OP_ACCEPT);
				
				// 循环遍历selector，每次休息的时间是1秒
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				
				Iterator<SelectionKey> it = selectedKeys.iterator();
				
//				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					System.out.println("key"+ key.toString());
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null)
								key.channel().close();
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws Exception {
		if (key.isValid()) {
			// 处理新接入的请求消息
			if (key.isAcceptable()) {
				// 根据SelectKey的操作位，来确定网络事件的类型
				// Accept the new connection
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				// 完成了accept操作相当于完成了tcp的三次的握手，tcp物理链路完全的建立起来了。
				sc.configureBlocking(false);
				// 在这里可以设置对tcp接受和发送缓冲区的大小？？？？
				// Add the new connection to the selector
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				// Read the data
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				// sc.read操作是非阻塞的
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					// 设置limit的位置为position，position为0
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("The time server receive order : " + body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
							? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					doWrite(sc, currentTime);
				} else if (readBytes < 0) {
					// 对端链路关闭
					key.cancel();
					sc.close();
				} else
					; // 读到0字节，忽略
			}
			
			if(key.isAcceptable()){
				System.out.println("key isAcceptable");
			}
			
			if(key.isConnectable()){
				System.out.println("key isConnectable.");
			}
		}

	}

	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			// 全双工的，能写能读
			channel.write(writeBuffer);
			// 写半包的场景
		}
	}
}