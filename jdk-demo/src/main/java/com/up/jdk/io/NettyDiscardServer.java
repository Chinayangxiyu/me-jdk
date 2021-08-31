package com.up.jdk.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @author: yangxiyu
 * @date: 2021/8/5 9:46 上午
 * @version: 1.0
 * @see
 */
public class NettyDiscardServer {

    private final int serverPort;

    public NettyDiscardServer(int serverPort) {
        this.serverPort = serverPort;
    }

    ServerBootstrap b = new ServerBootstrap();

    public static void main(String[] args) {
        NettyDiscardServer server = new NettyDiscardServer(9999);
        server.runServer();
    }

    public void runServer() {
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();
        try {
            // 1、设置反应器线程组
            b.group(bossLoopGroup, workerLoopGroup);
            // 2、设置NIO类型的通道
            b.channel(NioServerSocketChannel.class);
            // 3、设置监听端口
            b.localAddress(serverPort);
            // 4、设置通道参数
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//            b.childOption();
            // 5、装配子通道流水线
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettyDiscardHandler());
                    ch.pipeline().addLast(new NettyDiscardHandlerA());
                    ch.pipeline().addLast(new NettyDiscardHandlerB());
                }
            });


            ChannelFuture channelFuture = b.bind();
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }

    }

}
class NettyDiscardHandler extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        try {
//            ByteBuf in = (ByteBuf) msg;
//            while (in.isReadable()) {
//                System.out.print((char) in.readByte());
//            }
            System.out.println("======= source");
            super.channelRead(ctx, msg);
//        }finally {
//            ReferenceCountUtil.release(msg);
//        }
    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("out put source");

        String response = "out source";
        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
    }

}

class NettyDiscardHandlerA extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        try {
//            ByteBuf in = (ByteBuf) msg;
//            while (in.isReadable()) {
//                System.out.print((char) in.readByte());
//            }
            System.out.println("----- A");
            super.channelRead(ctx, msg);
            ctx.write("readA write ");

//        }finally {
//            ReferenceCountUtil.release(msg);
//        }
    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("out put A");

        String response = "out A";
        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
    }
}

class NettyDiscardHandlerB extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        try {
//            ByteBuf in = (ByteBuf) msg;
//            while (in.isReadable()) {
//                System.out.print((char) in.readByte());
//            }
            System.out.println("******* B");
            super.channelRead(ctx, msg);

//        }finally {
//            ReferenceCountUtil.release(msg);
//        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("out put B");
        String response = "out B";
        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
    }

}

