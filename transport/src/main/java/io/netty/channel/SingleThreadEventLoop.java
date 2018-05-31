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

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.concurrent.ThreadFactory;

/**
 * Abstract base class for {@link EventLoop}'s that execute all its submitted tasks in a single thread.
 *
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {

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
     * 
     * 客户端的channel也进行register的注册，这个注册时为了什么呢？
     * [id: 0x26e72531, /127.0.0.1:61499 => /127.0.0.1:8009]
     * */
    @Override
    public ChannelFuture register(Channel channel) {
    	//注册channel到Selector
    	log.info("{} register:{}",this.getClass().getName(),channel.toString());
        return register(channel, new DefaultChannelPromise(channel, this));
    }

    /**
     * NioEventLoopGroup在BootStrapServer的bind中，会先调用register方法
     * 而register方法，有NioEventLoopGroup相应，NioEventLoopGroup根据一定的规则
     * 选择child数组里面的NioEventLoop来进行处理。
     * */
    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }

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
