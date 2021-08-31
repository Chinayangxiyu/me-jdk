package com.up.jdk.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author: yangxiyu
 * @date: 2021/7/29 5:35 ����
 * @version: 1.0
 * @see
 */
public class SocketServerDemo {

    public static void main(String[] args) throws Exception{
        SocketServerDemo server = new SocketServerDemo();
        server.nioServerStart();
//        server.simpleServer();
    }

    /**
     * BIO 传统IO服务端
     * @throws Exception
     */
    public void simpleServer() throws Exception{

        ServerSocket server = new ServerSocket(9999);
        // 轮询等待连接请求
        while(true){
            Socket socket = server.accept();
            new Thread(() ->{
                try {
                    // 读取请求信息
                    InputStream is = socket.getInputStream();
                    byte[] container = new byte[64];
                    is.read(container);

                    System.out.println("========= request ========");
                    System.out.println(new String(container));

                    // 返回结果
                    OutputStream os = socket.getOutputStream();
                    os.write("response msg 11".getBytes());
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * NIO server 服务端
     * @throws Exception
     */
    public void nioServerStart() throws Exception{
        // 1、打开selector
        Selector selector = Selector.open();
        // 2、打开ServerSocketChannel
        ServerSocketChannel server = ServerSocketChannel.open();

        server.configureBlocking(false);// 设置非阻塞
        server.socket().bind(new InetSocketAddress( 9999));
        server.register(selector,  SelectionKey.OP_ACCEPT);// 注册accept事件

        while(selector.select() > 0){
            Iterator<SelectionKey> iterator= selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectKey = iterator.next();
                iterator.remove();//移除
                if(selectKey.isAcceptable()){
                    SocketChannel socketChannel = server.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);// 注册可读事件

                    // 可读时，读取数据
                }else if(selectKey.isValid() && selectKey.isReadable()){
                    SocketChannel socketChannel = (SocketChannel)selectKey.channel();

                    // 获取请求数据
                    System.out.println("=========== [request] ==========");
                    ByteBuffer requestBuffer = ByteBuffer.allocate(16);
                    int length;
                    StringBuilder requestBuilder = new StringBuilder();
                    // 如果没有读取到结束的标记（-1）则一直从流中读取数据
                    while((length= socketChannel.read(requestBuffer)) != -1){
                        byte[] cur = new byte[length];
                        //【重要】read()方法对于requestBuffer来说是写入，对应的position位置会变化，
                        // 我们调用get去读取数据时，需要调一次flip()，将写模式转为读模式
                        requestBuffer.flip();
                        requestBuffer.get(cur, 0, length);
                        String request = new String(cur, Charset.defaultCharset());
                        requestBuilder.append(request);
                        System.out.println(request);
                        requestBuffer.clear(); // 读取完后将读模式转为写模式，重复使用requestBuffer
                    }

                    duWrite(selectKey, requestBuilder.toString());
                    // 注册可写事件
//                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }else if(selectKey.isValid() && selectKey.isWritable()){
//                    ByteBuffer buffer = ByteBuffer.allocate(64);
//                    SocketChannel socketChannel = (SocketChannel)selectKey.channel();
//                    buffer.put("server response is size long out off".getBytes());
//                    // socketChannel.write对于buffer来说是调用buffer的读取方法，所以需要调一次flip
//                    buffer.flip();
//                    socketChannel.write(buffer);
//                    if (!buffer.hasRemaining()) {
//                        System.out.println("   server send response successes");
//                    }
//                    // 发送-1标记
//                    socketChannel.shutdownOutput();
//                    socketChannel.close();
                }
            }
        }
    }

    public void duWrite(SelectionKey selectKey, String request) throws Exception{
        ByteBuffer buffer = ByteBuffer.allocate(64);
        SocketChannel socketChannel = (SocketChannel)selectKey.channel();
        String response = "server response, request=" + request;
        buffer.put(response.getBytes());
        // socketChannel.write对于buffer来说是调用buffer的读取方法，所以需要调一次flip
        buffer.flip();
        socketChannel.write(buffer);
        if (!buffer.hasRemaining()) {
            System.out.println("   server send response successes");
        }
        // 发送-1标记
        socketChannel.shutdownOutput();
//        socketChannel.close();
    }
}
