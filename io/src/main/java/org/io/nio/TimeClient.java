package org.io.nio;

public class TimeClient {
    public static void main(String[] args) {
	int port = 8080;
	if (args != null && args.length > 0) {
	    try {
		port = Integer.valueOf(args[0]);
	    } catch (NumberFormatException e) {
		// 采用默认值
	    }
	}
	new TimeClientHandle("127.0.0.1", port).run();
    }
}
