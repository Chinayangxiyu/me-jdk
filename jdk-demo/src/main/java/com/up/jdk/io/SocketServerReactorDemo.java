package com.up.jdk.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 基于反应器模式的 NIO 服务端
 * @author: yangxiyu
 * @date: 2021/8/4 2:01 下午
 * @version: 1.0
 */
@Slf4j
public class SocketServerReactorDemo {

    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);


    public void startServer(){
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(9999));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            while(selector.select() > 0){
                Iterator<SelectionKey> keys =  selector.selectedKeys().iterator();
                while (keys.hasNext()){
                    SelectionKey key = keys.next();
                    keys.remove();
                    dispatch(key);
                }
            }

        }catch (IOException e){

        }
    }


    public void dispatch(SelectionKey key) throws IOException {

        if(key.isAcceptable()){
            doAccept(key);
        }else if(key.isReadable()){
            doRead(key);
        }
    }


    private void doAccept(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel)key.channel();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        SocketAddress remoteAddress = channel.getRemoteAddress();
        log.info("accept success remoteAddress={}", remoteAddress);
    }

    private void doRead(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel)key.channel();
        int length;
        // 如果没有读取到结束的标记（-1）则一直从流中读取数据
        while((length= channel.read(readBuffer)) != -1){
            byte[] cur = new byte[length];
            //【重要】read()方法对于requestBuffer来说是写入，对应的position位置会变化，
            // 我们调用get去读取数据时，需要调一次flip()，将写模式转为读模式
            readBuffer.flip();
            readBuffer.get(cur, 0, length);
            String request = new String(cur, Charset.defaultCharset());
            System.out.println(request);
            readBuffer.clear(); // 读取完后将读模式转为写模式，重复使用requestBuffer
        }
        // 注册可写事件
        channel.register(selector, SelectionKey.OP_WRITE);
    }

    public void doWrite(){
//        ByteBuffer buffer = ByteBuffer.allocate(64);
//        SocketChannel socketChannel = (SocketChannel)selectKey.channel();
//        buffer.put("server response is size long out off".getBytes());
//        // socketChannel.write对于buffer来说是调用buffer的读取方法，所以需要调一次flip
//        buffer.flip();
//        socketChannel.write(buffer);
//        if (!buffer.hasRemaining()) {
//            System.out.println("   server send response successes");
//        }
//        // 发送-1标记
//        socketChannel.shutdownOutput();
//        socketChannel.close();
    }



}
