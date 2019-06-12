/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.bootstrap;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * {@link Bootstrap} sub-class which allows easy bootstrap of
 * {@link ServerChannel}
 *
 */
public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {

	public static InternalLogger logger = InternalLoggerFactory.getInstance(ServerBootstrap.class);

	private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap<ChannelOption<?>, Object>();
	private final Map<AttributeKey<?>, Object> childAttrs = new LinkedHashMap<AttributeKey<?>, Object>();
	private volatile EventLoopGroup childGroup;
	private volatile ChannelHandler childHandler;

	public ServerBootstrap() {
		logger.info("Netty Server construct begine:  "+ DateFormat.getDateTimeInstance().format(new Date()));
	}

	private ServerBootstrap(ServerBootstrap bootstrap) {
		super(bootstrap);
		childGroup = bootstrap.childGroup;
		childHandler = bootstrap.childHandler;
		synchronized (bootstrap.childOptions) {
			childOptions.putAll(bootstrap.childOptions);
		}
		synchronized (bootstrap.childAttrs) {
			childAttrs.putAll(bootstrap.childAttrs);
		}
	}

	/**
	 * Specify the {@link EventLoopGroup} which is used for the parent
	 * (acceptor) and the child (client).
	 */
	@Override
	public ServerBootstrap group(EventLoopGroup group) {
		return group(group, group);
	}

	/**
	 * Set the {@link EventLoopGroup} for the parent (acceptor) and the child
	 * (client). These {@link EventLoopGroup}'s are used to handle all the
	 * events and IO for {@link ServerChannel} and {@link Channel}'s.
	 */
	public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
		super.group(parentGroup);
		if (childGroup == null) {
			throw new NullPointerException("childGroup");
		}
		if (this.childGroup != null) {
			throw new IllegalStateException("childGroup set already");
		}
		this.childGroup = childGroup;
		return this;
	}

	/**
	 * Allow to specify a {@link ChannelOption} which is used for the
	 * {@link Channel} instances once they get created (after the acceptor
	 * accepted the {@link Channel}). Use a value of {@code null} to remove a
	 * previous set {@link ChannelOption}.
	 */
	public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value) {
		if (childOption == null) {
			throw new NullPointerException("childOption");
		}
		if (value == null) {
			synchronized (childOptions) {
				childOptions.remove(childOption);
			}
		} else {
			synchronized (childOptions) {
				childOptions.put(childOption, value);
			}
		}
		return this;
	}

	/**
	 * Set the specific {@link AttributeKey} with the given value on every child
	 * {@link Channel}. If the value is {@code null} the {@link AttributeKey} is
	 * removed
	 */
	public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value) {
		if (childKey == null) {
			throw new NullPointerException("childKey");
		}
		if (value == null) {
			childAttrs.remove(childKey);
		} else {
			childAttrs.put(childKey, value);
		}
		return this;
	}

	/**
	 * Set the {@link ChannelHandler} which is used to serve the request for the
	 * {@link Channel}'s.
	 */
	public ServerBootstrap childHandler(ChannelHandler childHandler) {
		if (childHandler == null) {
			throw new NullPointerException("childHandler");
		}
		this.childHandler = childHandler;
		return this;
	}

	/**
	 * Return the configured {@link EventLoopGroup} which will be used for the
	 * child channels or {@code null} if non is configured yet.
	 */
	public EventLoopGroup childGroup() {
		return childGroup;
	}

	/**
	 * 此接口仅仅是初始化
	 * 
	 *  1. 初始化channe 
	 *  2. 初始化channel的Pipeline 
	 *  3. 初始化Pipeline上面的handler:处理链接接入的一次性的hander：ChannelInitializer，逻辑中pipeline加入了：ServerBootstrapAcceptor
	 */
	@Override
	void init(Channel channel) throws Exception {
		// 第一步：初始化Channel的option
		final Map<ChannelOption<?>, Object> options = options();
		synchronized (options) {
			channel.config().setOptions(options);
		}

		// 第二步：Channel的属性attr
		final Map<AttributeKey<?>, Object> attrs = attrs();
		synchronized (attrs) {
			for (Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
				@SuppressWarnings("unchecked")
				AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
				channel.attr(key).set(e.getValue());
			}
		}

		/**
		 * 第三步构建ChannelPipeline的构建，这个是channel上面的“管道”
		 * 这里的pipeline()相当于get方法，为channel的一个属性： private final DefaultChannelPipeline(this); //this:传入的channel实例。
		 * */
		ChannelPipeline p = channel.pipeline();
		if (handler() != null) {
			
			/**
			 * 这里的handler和childhandler的区别？ 都是在启动的Server的时候，设置的handler(class实例)
			 * handler：ChannelHandler
			 * addLast: 把handler插入DefaultChannelPipeline的tail handler之前
			 */
			p.addLast(handler());
		}

		final EventLoopGroup currentChildGroup = childGroup;
		// ServerBootStrap的childHandler方法，指定了 childHandler
		final ChannelHandler currentChildHandler = childHandler;

		/**
		 * 这里面有两个handler，因为ServerBootStrap继承自：AbstractBootstrap
		 * AbstractBootstrap 中有一个成员变量：volatile ChannelHandler handler
		 * ServerBootStrap 有一个成员变量： volatile ChannelHandler childHandler
		 * */

		final Entry<ChannelOption<?>, Object>[] currentChildOptions;
		final Entry<AttributeKey<?>, Object>[] currentChildAttrs;
		synchronized (childOptions) {
			currentChildOptions = childOptions.entrySet().toArray(newOptionArray(childOptions.size()));
		}
		synchronized (childAttrs) {
			currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(childAttrs.size()));
		}

		// 这个ChannelInitializer，中的iniChannel，是什么时候执行呢？
		
		/**
		 *  ChannelInitializer 非常的有意思:
		 *  extends ChannelInboundHandlerAdapter，重载的是：channelRegistered(ChannelHandlerContext ctx)
		 *  具体的逻辑就是：
		 *  1. initChannel((C) ctx.channel());  // 调用抽象方法，这个有具体的子类来进行实现，初始化channel
		 *  2. pipeline.remove(this); // 然后从pipeline中把this，也就是这个ChannelInitializer 对应的实例，删除了。
		 *  从逻辑上面说，也属于正常，因为这个是类的目的就是为了初始化channel，完成任务以后，以为初始化操作只执行了一次，所以
		 *  直接的删除了。
		 *  3. ctx.fireChannelRegistered(); // fire channel已经注册完成的事件。
		 *  
		 * */
		ChannelInitializer<Channel> ini = new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(Channel ch) throws Exception {
				logger.info("II. 增加ServerBootstrapAcceptor 到pipeline 中。 !");
				/**
				 * channel的初始化的过程逻辑：handler中被加入了：ServerBootstrapAcceptor
				 */
				ch.pipeline().addLast(new ServerBootstrapAcceptor(currentChildGroup, currentChildHandler,
						currentChildOptions, currentChildAttrs));
			}
		};
		
		/**
		 * ini 是作为一个handler加入了DefaultChannelPipeline，但是具体的类型是一个匿名类，实现的是ChannelInitializer
		 * */
		p.addLast(ini);
	}

	@Override
	public ServerBootstrap validate() {
		super.validate();
		if (childHandler == null) {
			throw new IllegalStateException("childHandler not set");
		}
		// parentGroup 在上面已经校验过了，如果childHandler没有设置的话，parentGroup替代
		if (childGroup == null) {
			logger.warn("childGroup is not set. Using parentGroup instead.");
			childGroup = group();
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	private static Entry<ChannelOption<?>, Object>[] newOptionArray(int size) {
		return new Entry[size];
	}

	@SuppressWarnings("unchecked")
	private static Entry<AttributeKey<?>, Object>[] newAttrArray(int size) {
		return new Entry[size];
	}

	/**
	 * 这个内部类，可以看到做是处理客户端connect事件的内部的handler。
	 * 
	 */
	private static class ServerBootstrapAcceptor extends ChannelInboundHandlerAdapter {

		private final EventLoopGroup childGroup;
		private final ChannelHandler childHandler;
		private final Entry<ChannelOption<?>, Object>[] childOptions;
		private final Entry<AttributeKey<?>, Object>[] childAttrs;

		ServerBootstrapAcceptor(EventLoopGroup childGroup, ChannelHandler childHandler,
				Entry<ChannelOption<?>, Object>[] childOptions, Entry<AttributeKey<?>, Object>[] childAttrs) {
			this.childGroup = childGroup;
			this.childHandler = childHandler;
			this.childOptions = childOptions;
			this.childAttrs = childAttrs;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			final Channel child = (Channel) msg;
			logger.info("ServerBootstrapAcceptor channel type: {}",child.getClass().getName());
			child.pipeline().addLast(childHandler);
			for (Entry<ChannelOption<?>, Object> e : childOptions) {
				try {
					if (!child.config().setOption((ChannelOption<Object>) e.getKey(), e.getValue())) {
						logger.warn("Unknown channel option: " + e);
					}
				} catch (Throwable t) {
					logger.warn("Failed to set a channel option: " + child, t);
				}
			}

			for (Entry<AttributeKey<?>, Object> e : childAttrs) {
				child.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
			}

			try {
				// 同样的逻辑NioEventLoopGroup提供的register，但是这里的NioEventLoopGroup，表示的是worker线程组。
				// 这里就把NioSocketChannel注册到代表着
				// worker事件组的：NioEventLoopGroup下面的某一个NioEventLoop
				logger.info("channel:{} ===============register============> childGroup:{} ",child,childGroup);
				ChannelFuture future = childGroup.register(child);
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							forceClose(child, future.cause());
						}
					}
				});
			} catch (Throwable t) {
				forceClose(child, t);
			}
		}

		private static void forceClose(Channel child, Throwable t) {
			child.unsafe().closeForcibly();
			logger.warn("Failed to register an accepted channel: " + child, t);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			final ChannelConfig config = ctx.channel().config();
			if (config.isAutoRead()) {
				// stop accept new connections for 1 second to allow the channel
				// to recover
				// See https://github.com/netty/netty/issues/1328
				config.setAutoRead(false);
				ctx.channel().eventLoop().schedule(new Runnable() {
					@Override
					public void run() {
						config.setAutoRead(true);
					}
				}, 1, TimeUnit.SECONDS);
			}
			// still let the exceptionCaught event flow through the pipeline to
			// give the user
			// a chance to do something with it
			ctx.fireExceptionCaught(cause);
		}
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public ServerBootstrap clone() {
		return new ServerBootstrap(this);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		buf.setLength(buf.length() - 1);
		buf.append(", ");
		if (childGroup != null) {
			buf.append("childGroup: ");
			buf.append(StringUtil.simpleClassName(childGroup));
			buf.append(", ");
		}
		synchronized (childOptions) {
			if (!childOptions.isEmpty()) {
				buf.append("childOptions: ");
				buf.append(childOptions);
				buf.append(", ");
			}
		}
		synchronized (childAttrs) {
			if (!childAttrs.isEmpty()) {
				buf.append("childAttrs: ");
				buf.append(childAttrs);
				buf.append(", ");
			}
		}
		if (childHandler != null) {
			buf.append("childHandler: ");
			buf.append(childHandler);
			buf.append(", ");
		}
		if (buf.charAt(buf.length() - 1) == '(') {
			buf.append(')');
		} else {
			buf.setCharAt(buf.length() - 2, ')');
			buf.setLength(buf.length() - 1);
		}

		return buf.toString();
	}
}
