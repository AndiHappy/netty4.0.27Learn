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
package io.netty.channel;

import java.util.concurrent.ThreadFactory;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Abstract base class for {@link EventLoopGroup} implementations that handles their tasks with multiple threads at
 * the same time.
 */
//EventLoopGroup基础功能的实现
public abstract class MultithreadEventLoopGroup extends MultithreadEventExecutorGroup implements EventLoopGroup {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultithreadEventLoopGroup.class);

    //默认的worker的数量，CPU*2
    private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
        }
    }

    /**
     * @see {@link MultithreadEventExecutorGroup#MultithreadEventExecutorGroup(int, ThreadFactory, Object...)}
     */
    protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads == 0? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
    }

    @Override
    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass(), Thread.MAX_PRIORITY);
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    /**
     * channel 为首先新建立的NioServerSocketChannel
     * 
     * register(channel),到底是channel注册到LoopGroup，还是LoopGroup注册到channel？
     * 
     * 回答：Channel注册到LoopGroup的Selector上面去。
     * 
     * 另外这里的regist为NioEventLoopGroup的成员函数，在netty中调用了两次：
     * 
     * ChannelFuture regFuture = group().register(channel);
     * ChannelFuture future = childGroup.register(child);
     * 
     * 从ServerBootStrap的设置中知晓：group为bossgroup，worker为workergroup
     * 从上面register的注册可以知道，register的逻辑就是channel/child 注册到NioEventLoopGroup的Selector
     * 那么可不可以理解为server端的channel全部注册在parent或者称之为group的NioEventLoopGroup中，
     * 但是客户端连接进来的Channel全部注册在worker或称之为child的NioEventLoopGroup中，
     * 
     * 如果是这样的话，那么他们之间的交互，又该是如何交互的？
     * 
     * 回答：
     * 	Selector的SelectKey可以设置感兴趣事件：key.interestOps(SelectionKey.OP_ACCEPT);
     * 
     *  需要找到selector的设置SelectionKey.OP_ACCEPT的代码点：
     *  
     *  
     * */
    @Override
    public ChannelFuture register(Channel channel) {
    	/**
    	 * next() 根据 nThreads是否为2的次方选择具体的哪一个children,返回的实例是NioEventLoop类型
    	 * 
    	 * nThreads 是在ServerBootStrap 设置的NioEventLoopGroup() 的线程数
    	 * 
    	 * next() 保证的是注册的时候，channel的注册事件被均匀的分布到NioEventLoop,具体怎么保证每一个channel能够一一的对应，这个不在这个里面。
    	 * 
    	 * NioEventLoop.register(NioServerSocketChannel)
    	 * */
        return next().register(channel);
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return next().register(channel, promise);
    }
}
