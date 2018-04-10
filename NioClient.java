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
	  // ͨ��������
	  private Selector selector;
	  /**
	   * ���һ��Socketͨ�������Ը�ͨ����һЩ��ʼ���Ĺ���
	   * 
	   * @param ip ���ӵķ�������ip
	   * @param port ���ӵķ������Ķ˿ں�
	   * @throws IOException
	   */
	  public void initClient(String ip, int port) throws IOException {
		    // ���һ��Socketͨ��
		    SocketChannel channel = SocketChannel.open();
		    // ����ͨ��Ϊ������
		    channel.configureBlocking(false);
		    // ���һ��ͨ��������
		    this.selector = Selector.open();
		    // �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ��listen���������е�
		    // ��channel.finishConnect();�����������
		    channel.connect(new InetSocketAddress(ip, port));
		    // ��ͨ���������͸�ͨ���󶨣���Ϊ��ͨ��ע��SelectionKey.OP_CONNECT�¼���
		    channel.register(selector, SelectionKey.OP_CONNECT);
	  }
	  /**
	   * ������ѯ�ķ�ʽ����selector���Ƿ�����Ҫ������¼�������У�����д���
	   * 
	   * @throws IOException
	 * @throws InterruptedException 
	   */
	  public void listen() throws IOException, InterruptedException {
	    System.out.println("nio client liesten start.");
	    // ��ѯ����selector
	    while (true) 
	    {
		      // ��ע����¼�����ʱ���������أ�����,�÷�����һֱ����
		      selector.select();
		      // ���selector��ѡ�е���ĵ�������ѡ�е���Ϊע����¼�
		      Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		      while (it.hasNext()) {
			        SelectionKey key = it.next();
			        // ɾ����ѡ��key,�Է��ظ�����
			        it.remove();
			        // �����¼�����
			        if (key.isConnectable()) {
				          SocketChannel channel = (SocketChannel) key.channel();
				          // ����������ӣ����������
				          if (channel.isConnectionPending()) {
				            channel.finishConnect();
				          }
				          // ���÷�����
				          channel.configureBlocking(false);
				          // ��������˷�����Ϣ
				          System.out.println("client send: " + cnt);
				          channel.write(ByteBuffer.wrap(new String("" + cnt).getBytes()));
				          // �ںͷ�������ӳɹ�֮��Ϊ�˿��Խ��յ�����˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
				          channel.register(selector, SelectionKey.OP_READ);
				          // ����˿ɶ����¼�
			        } else if (key.isReadable()) {
			        	 read(key);
			        }
		      }
	    }
	  }
	  /**
	   * �����ȡ�������˷�������Ϣ ���¼�
	   * 
	   * @param key
	   * @throws IOException
	 * @throws InterruptedException 
	   */
	  public void read(SelectionKey key) throws IOException, InterruptedException {
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
	    System.out.println(msg);
	    System.out.println("---------------------------------------------------------");
	    Thread.sleep(1000);
	    System.out.println("client send: " + (++cnt));
	    ByteBuffer outBuffer = ByteBuffer.wrap(new String("" + cnt).getBytes());
	    outBuffer.clear();
	    channel.write(outBuffer);
	  }
	  // �ͻ��˲���
	  public static void main(String[] args) throws IOException, InterruptedException {
	    NioClient nioClient = new NioClient();
	    nioClient.initClient("127.0.0.1", 8000);
	    nioClient.listen();
	  }
}
