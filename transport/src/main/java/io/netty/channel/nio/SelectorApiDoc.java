package io.netty.channel.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class SelectorApiDoc  extends Selector{

	/***
	 * 判断selector是否打开
	 * 
	 * Selector有2种创建方式：
     * Selector.open():会调用操作系统底层的Selector实现来创建Selector；
     * SelectorProvider.openSelector()：这种方式直接调用SelectorProvider类来获取操作系统底层的Selector来创建Selector。
	 * */
	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public SelectorProvider provider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SelectionKey> keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int selectNow() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int select(long timeout) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int select() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Selector wakeup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
