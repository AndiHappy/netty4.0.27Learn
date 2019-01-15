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
package io.netty.channel.nio;

import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.util.NoteLog;
import io.netty.util.concurrent.EventExecutor;

/***
 * 
 * EventLoop的是处理事件的循环，接入，读，写，accept等事件的处理，形成一个循环，持续的提供事件的处理机制。
 * 
 * 然后就是事件的来源，这个是源头：Selector的三个集合：
 * 
 * key集合: 注册到Selector的SelectableChannel对应的SelectionKey，这个有个点，就是Channel需要注册到这个Selector上面，注册到这个Selector的上面，可以说事件的开端。
 * selected-key集合: Selector会定期向操作系统内核询问是否有事件要通知到SelectableChannel,会将那些对这些事件感兴趣的SelectableChannel当时注册到Selector的SelectionKey复制一份到此集合，
 * cancelled-key集合: 那些取消了对操作系统内核事件关注但并未取消对于Selector注册的SelectableChannel对应的SelectionKey的集合
 * 
 * 
 * NioEventLoopGroup 的对外的展示：
 * 
 * 构造函数：
 * 
 * public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, final SelectorProvider selectorProvider)
 * 
 * 设置处理IO任务和非IO任务的时间比例,默认的为50
 * public void setIoRatio(int ioRatio)  
 * 
 * 重新构建Selector
 * public void rebuildSelectors()
 * 
 * 新建的child,这个是事件处理机制
 * protected EventExecutor newChild(ThreadFactory threadFactory, Object... args)
 * 
 * 
 * */
public class NioEventLoopGroup extends MultithreadEventLoopGroup {

	/**
	 * 
	 * 类名翻译过来就是：事件循环组
	 * 这个注释目的是确定NioEventLoopGroup在初始化的过程中，做了哪些事情
	 * 1. 设置线程工厂是：newDefaultThreadFactory
	 * 2.0 根据children的数量，确定chooser
	 * 2. 确定children：NioEventLoop，初始化NioEventLoop
	 *   2.1 关于NioEventLoop的加载的逻辑
	 * 3. 增加监听，监听NioEventLoop的停止
	 * */
    public NioEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new instance using the specified number of threads, {@link ThreadFactory} and the
     * {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
     */
    public NioEventLoopGroup(int nThreads) {
        this(nThreads, null);
    }

    //可以说所有的NioEventLoopGroup里面的NioEventLoop对应的全部是同一个selectorProvider
    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, SelectorProvider.provider());
    }

   
    /**
     * nThreads: child的数量
     * threadFactory：默认为null
     * selectorProvider：关注SelectorProvider的DOC：Service-provider class for selectors and selectable channels.
     * 这个类主要的是
     * */
    public NioEventLoopGroup(
            int nThreads, ThreadFactory threadFactory, final SelectorProvider selectorProvider) {
        super(nThreads, threadFactory, selectorProvider);
    }

    /**
     * Sets the percentage of the desired amount of time spent for I/O in the child event loops.  
     * The default value is {@code 50}, 
     * which means the event loop will try to spend the same amount of time for I/O as for non-I/O tasks.
     *
     * I/O 任务和 非I/O 的任务的比例
     */
    public void setIoRatio(int ioRatio) {
        for (EventExecutor e: children()) {
            ((NioEventLoop) e).setIoRatio(ioRatio);
        }
    }

    /**
     * Replaces the current {@link Selector}s of the child event loops with newly 
     * created {@link Selector}s to work
     * around the  infamous epoll 100% CPU bug.
     */
    public void rebuildSelectors() {
        for (EventExecutor e: children()) {
            ((NioEventLoop) e).rebuildSelector();
        }
    }

    /**
     * NioEventLoopGroup 的重载的方法，：指定Child的类型，NioEventLoop
     * 
     * 这个就是Group下面的children，初始化child数组的时候调用，外层的调用就是在new NioEventLoopGroup的时候
     * */
    @Override
    protected EventExecutor newChild(
            ThreadFactory threadFactory, Object... args) throws Exception {
        return new NioEventLoop(this, threadFactory, (SelectorProvider) args[0]);
    }
}
