package com.up.jdk.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SocketClientDemo {

    private static final int SOCKET_TIMEOUT = 3000;
    private static final int CONNECT_TIMEOUT = 3000;

    public static void main(String[] args) throws Exception{
        SocketClientDemo demo = new SocketClientDemo();

        for (int i = 0; i < 20; i++){
            demo.startBioClient(" " + i);
        }
//        demo.startClient();
    }


    /**
     *
     * @throws Exception
     */
    public  void startBioClient(String msg) throws Exception{
        Socket socket = new Socket();
        socket.setSoTimeout(SOCKET_TIMEOUT);
        socket.connect(new InetSocketAddress("127.0.0.1", 9999), CONNECT_TIMEOUT);
        // 客户端请求数据
        OutputStream os = socket.getOutputStream();
        String request = "request msg" + msg;
        os.write(request.getBytes());
        socket.shutdownOutput();

        // 服务端回复数据
        InputStream is = socket.getInputStream();
        byte[] buffer = new byte[16];
        int length = 0;
        System.out.println("=======response =======");
        while((length = is.read(buffer)) != -1){ // 一直读到结束标记（-1）
            System.out.println(new String(buffer, 0, length));
        }
        os.close();
        is.close();
        socket.close();
    }
    /**
     *
     * @throws Exception
     */
    public  void startClient() throws Exception{

        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        SocketAddress address = new InetSocketAddress("127.0.0.1", 9000);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(address);
        while (!socketChannel.finishConnect()){
            // �ȴ�һ��� �ȴ����Ӵ����ɹ�
            Thread.sleep(100L);
        }

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()){
            SelectionKey key = iterator.next();
            iterator.remove();

            if(key.isConnectable()){

                SocketChannel socketChannel1 = (SocketChannel)key.channel();
                if(socketChannel1.isConnectionPending()){
                    socketChannel1.finishConnect();
                }
                socketChannel1.configureBlocking(false);
                socketChannel1.write(ByteBuffer.wrap("request msg".getBytes()));
                socketChannel1.register(selector, SelectionKey.OP_READ);
            }else if(key.isReadable()){
                SocketChannel socketChannel1 = (SocketChannel)key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(32);
                socketChannel1.read(buffer);
                String response = new String(buffer.array());
                System.out.println("========= response =======");
                System.out.println(response);
            }
        }
    }

}
