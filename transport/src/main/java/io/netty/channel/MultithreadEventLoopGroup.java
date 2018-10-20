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

import io.netty.channel.nio.NioEventLoop;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

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
     * */
    @Override
    public ChannelFuture register(Channel channel) {
    	//next(),选择的是new NioEventLoopGroup(int nThreads)，里面的nThreads，对应的就是children：SingleThreadEventLoop数组
    	//实际的新建在NioEventLoopGroup中，对应的是：new NioEventLoop(this, threadFactory, (SelectorProvider) args[0])
        //next()根据nThreads是否为2的次方选择具体的哪一个children,实例就是NioEventLoop类型
    	//register为SingleThreadEventLoop成员函数
        return next().register(channel);
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return next().register(channel, promise);
    }
}
