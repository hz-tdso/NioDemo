package niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient {
	private int cnt = 0;
	  // 通道管理器
	  private Selector selector;
	  /**
	   * 获得一个Socket通道，并对该通道做一些初始化的工作
	   * 
	   * @param ip 连接的服务器的ip
	   * @param port 连接的服务器的端口号
	   * @throws IOException
	   */
	  public void initClient(String ip, int port) throws IOException {
		    // 获得一个Socket通道
		    SocketChannel channel = SocketChannel.open();
		    // 设置通道为非阻塞
		    channel.configureBlocking(false);
		    // 获得一个通道管理器
		    this.selector = Selector.open();
		    // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		    // 用channel.finishConnect();才能完成连接
		    channel.connect(new InetSocketAddress(ip, port));
		    // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		    channel.register(selector, SelectionKey.OP_CONNECT);
	  }
	  /**
	   * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	   * 
	   * @throws IOException
	 * @throws InterruptedException 
	   */
	  public void listen() throws IOException, InterruptedException {
	    System.out.println("nio client liesten start.");
	    // 轮询访问selector
	    while (true) 
	    {
		      // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
		      selector.select();
		      // 获得selector中选中的项的迭代器，选中的项为注册的事件
		      Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		      while (it.hasNext()) {
			        SelectionKey key = it.next();
			        // 删除已选的key,以防重复处理
			        it.remove();
			        // 连接事件发生
			        if (key.isConnectable()) {
				          SocketChannel channel = (SocketChannel) key.channel();
				          // 如果正在连接，则完成连接
				          if (channel.isConnectionPending()) {
				            channel.finishConnect();
				          }
				          // 设置非阻塞
				          channel.configureBlocking(false);
				          // 向服务器端发送信息
				          System.out.println("client send: " + cnt);
				          channel.write(ByteBuffer.wrap(new String("" + cnt).getBytes()));
				          // 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
				          channel.register(selector, SelectionKey.OP_READ);
				          // 获得了可读的事件
			        } else if (key.isReadable()) {
			        	 read(key);
			        }
		      }
	    }
	  }
	  /**
	   * 处理读取服务器端发来的信息 的事件
	   * 
	   * @param key
	   * @throws IOException
	 * @throws InterruptedException 
	   */
	  public void read(SelectionKey key) throws IOException, InterruptedException {
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
	    System.out.println(msg);
	    System.out.println("---------------------------------------------------------");
	    Thread.sleep(1000);
	    System.out.println("client send: " + (++cnt));
	    ByteBuffer outBuffer = ByteBuffer.wrap(new String("" + cnt).getBytes());
	    outBuffer.clear();
	    channel.write(outBuffer);
	  }
	  // 客户端测试
	  public static void main(String[] args) throws IOException, InterruptedException {
	    NioClient nioClient = new NioClient();
	    nioClient.initClient("127.0.0.1", 8000);
	    nioClient.listen();
	  }
}
