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
import java.util.logging.Logger;

import io.netty.util.NoteLog;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;

/**
 * Abstract base class for {@link EventLoop}'s that execute all its submitted tasks in a single thread.
 *
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {

    private static final Logger log = Logger.getLogger(SingleThreadEventLoop.class.getName());

    /**
     * @see {@link SingleThreadEventExecutor#SingleThreadEventExecutor(EventExecutorGroup, ThreadFactory, boolean)}
     */
    protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
        super(parent, threadFactory, addTaskWakesUp);
    }

    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    /**
     * channel为NioServerSocketChannel,这个方法执行的时候属于实例NioEventLoop，有一个SelectorProvider
     * */
    @Override
    public ChannelFuture register(Channel channel) {
    	log.info("2.1 Channel register 成员函数所在执行实例:"+this+" 通道实例: " + channel);
        return register(channel, new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }
        
        /**
         * 这个地方有很大的疑问：
         * channel.unsafe() 是NioServerSocketChannel的父类中的一个成员变量，在创建NioServerSocketChannel的时候，这个
         * 成员变量已经被赋值了： new NioMessageUnsafe().所以unsafe返回的就是NioMessageUnsafe实例，这个是对“channel的
         * 另外的一次封装”。此处存疑。
         * 
         * NioMessageUnsafe的成员函数register方法是：register(EventLoop eventLoop, final ChannelPromise promise)
         * 这里的EventLoop就是NioEventLoop，拥有一个selectprovider的NioEventLoop。
         * 
         * 具体的实现的逻辑在：AbstractUnsafe中。
         * 
         * NioMessageUnsafe 《==  AbstractNioUnsafe 《== AbstractUnsafe 《==implements Unsafe
         * 
         * channel.unsafe() 返回的是新建NioServerSocketChannel的时候，会设置的 NioMessageUnsafe
         * 
         *  channel.unsafe().register(this, promise) 会直接的进入NioMessageUnsafe的方法中！
         * */
        
        channel.unsafe().register(this, promise);
        return promise;
    }

    @Override
    protected boolean wakesUpForTask(Runnable task) {
        return !(task instanceof NonWakeupRunnable);
    }

    /**
     * Marker interface for {@linkRunnable} that will not trigger an {@link #wakeup(boolean)} in all cases.
     */
    interface NonWakeupRunnable extends Runnable { }
}
