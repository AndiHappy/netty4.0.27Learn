package io.netty.example.discard;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author zhailz
 *
 * @version 2018年5月29日 下午12:12:41
 */
public class ChildHandler extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ChannelPipeline p = ch.pipeline();
         p.addLast(new DiscardServerHandler());
		
	}

}
