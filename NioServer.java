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
		serverSocketChannel.configureBlocking(false);//������ģʽ
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
				// �ͻ������������¼�
		        if (key.isAcceptable()) {
			          ServerSocketChannel server = (ServerSocketChannel) key.channel();
			          // ��úͿͻ������ӵ�ͨ��
			          SocketChannel channel = server.accept();//serversocketchannel�ڷ�����ģʽ��accept��������������
			          if(null!=channel){
					          // ���óɷ�����
					          channel.configureBlocking(false);
		//			          // ��������Ը��ͻ��˷�����Ϣ
		//			          channel.write(ByteBuffer.wrap(new String("server accepted\n").getBytes()));
					          // �ںͿͻ������ӳɹ�֮��Ϊ�˿��Խ��յ��ͻ��˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
					          channel.register(selector, SelectionKey.OP_READ);
			          }
			    // ����˿ɶ��¼�
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
	    // �������ɶ�ȡ��Ϣ:�õ��¼�������Socketͨ��
	    SocketChannel channel = (SocketChannel) key.channel();
	    // ������ȡ�Ļ�����
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
	    // ����Ϣ���͸��ͻ���
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
