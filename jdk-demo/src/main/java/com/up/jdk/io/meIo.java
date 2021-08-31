package com.up.jdk.io;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * @author yxy
 * @date 2020/4/24 11:29
 * @description
 */
public class meIo {

    public static void main(String[] args) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            FileChannel fileChannel = new FileInputStream("C:\\Users\\yxy\\Desktop\\编码测试.txt").getChannel();
            while(fileChannel.read(buffer) != -1){
                buffer.flip();
                System.out.println(
                        Charset.forName("ISO-8859-1").decode(buffer));

                buffer.clear();
            }
//            CharBuffer buffer = CharBuffer.allocate(1);
//            FileInputStream fis = new FileInputStream("C:\\Users\\yxy\\Desktop\\编码测试.txt");
//            InputStreamReader reader = new InputStreamReader(fis);
//            reader.read(buffer);
//
//            System.out.println(buffer.toString());



//            System.out.println(buffer.asCharBuffer());
//
//            String encoding = System.getProperty("file.encoding");
//            System.out.println("Decoded using " + encoding + ": " +
//            Charset.forName("GB2312").decode(buffer));
//
//            SocketChannel socketChannel = SocketChannel.open();
//            socketChannel.register()
//
//            Charset.
//            CharBuffer charBuffer = GBK.   decode(buffer);
//            CharBuffer charBuffer = buffer.asCharBuffer();
////            charBuffer.flip();
//
//            while(charBuffer.position() < charBuffer.limit()){
//
//                System.out.println(buffer.getChar());
//
//            }
//            System.out.println(buffer);

//            buffer.rewind();
//            buffer.getChar()

        }catch (Exception e){

        }

    }

    public void testIO(){
        PipedInputStream pis = new PipedInputStream();

//
//        ByteArrayOutputStream reader = new ByteArrayOutputStream(pis);
//        reader.toByteArray();
//
//        ByteArrayInputStream

//        Reader reader = new FileReader();Reader();
//        BufferedReader reader1 = new BufferedReader();
//        CharArrayReader charArrayReader = new CharArrayReader()
////        charArrayReader.read();
//                FileWriter fileWriter = new FileWriter();
//                FileReader fileReader = new FileReader();
//        fileWriter.write();
//
//        InputStreamReader inputStreamReader = new InputStreamReader();
//        BufferedReader bufferedReader = new BufferedReader();
//        PrintWriter printWriter = new PrintWriter();
//        StringReader stringReader = new StringReader("aaaaa");
//        stringReader.read();
//        StringWriter stringWriter = new StringWriter();
//        stringWriter.write();
    }

    public void testNio() throws Exception{
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        FileChannel fileChannel = new FileOutputStream("aa").getChannel();
//        buffer.flip();
//        fileChannel.write(buffer);
//
//
//        FileOutputStream fos = new FileOutputStream("");
//        fos.getChannel();
//        // 调用本地方法一个字节一个字节的写
//
//
////        Selector.select();
//
////        WindowsSelectorImpl selector = new WindowSelectorImpl();
//
//        Selector selector = Selector.open();
//
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);
//        channel.configureBlocking();
//
//        FileInputStream
    }
}
