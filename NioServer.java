package niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServer {
	private Selector selector;
	
	public void initServer(int port) throws IOException{
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);//非阻塞模式
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	public void listen() throws IOException{
		System.out.println("nio server listen start.");
		while(true){
			selector.select();
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				// 客户端请求连接事件
		        if (key.isAcceptable()) {
			          ServerSocketChannel server = (ServerSocketChannel) key.channel();
			          // 获得和客户端连接的通道
			          SocketChannel channel = server.accept();//serversocketchannel在非阻塞模式下accept方法会立即返回
			          if(null!=channel){
					          // 设置成非阻塞
					          channel.configureBlocking(false);
		//			          // 在这里可以给客户端发送信息
		//			          channel.write(ByteBuffer.wrap(new String("server accepted\n").getBytes()));
					          // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
					          channel.register(selector, SelectionKey.OP_READ);
			          }
			    // 获得了可读事件
		        } else if (key.isReadable()) {
		        		read(key);
		        }
			}
		}
	}
	
//	private void write(SelectionKey key) throws IOException {
//		SocketChannel channel = (SocketChannel) key.channel();
//		channel.write(ByteBuffer.wrap("abc".getBytes()));
//	}

	public void read(SelectionKey key) throws IOException {
	    // 服务器可读取消息:得到事件发生的Socket通道
	    SocketChannel channel = (SocketChannel) key.channel();
	    // 创建读取的缓冲区
	    ByteBuffer buffer = ByteBuffer.allocate(1024);
	    buffer.clear();
	    channel.read(buffer);
	    int pos = buffer.position();
	    buffer.flip();
	    byte[] data = new byte[pos];
	    int i=0;
	    while(buffer.hasRemaining()){
	    	data[i++] = buffer.get();
	    }
	    String msg = new String(data);
//	    System.out.println("server received: " + msg);
	    // 将消息回送给客户端
	    ByteBuffer outBuffer = ByteBuffer.wrap(new String("server echo: " + msg + "\n").getBytes());
	    outBuffer.clear();
	    channel.write(outBuffer);
	}
	
	public static void main(String[] args) throws IOException {
	    NioServer nioServer = new NioServer();
	    nioServer.initServer(8000);
	    nioServer.listen();
	 }
}
