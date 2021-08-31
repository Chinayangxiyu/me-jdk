# 一、IO原理分析
&emsp;&emsp;    什么是IO：Input/Output，数据的输入、输出，说到底就是数据的传输。
&emsp;&emsp;    IO数据：对于计算机来说，数据最终存放在两个地方，内存、磁盘；所以IO可以理解为对内存、磁盘上的数据进行输入、输出操作；  
&emsp;&emsp;    数据格式：计算机中数据的存储和传输都是二进制的格式，我们所谓传递的“字符串”实际也是二进制格式进行传输，只是接收端、发送端进行了编码；  
&emsp;&emsp;    数据传输：任何事务都不能实现瞬移，数据传输也一样，是像水流一样，一点一点传输的，所以抽象为流。
                
#java IO
（1）IO是在内存、磁盘上对二进制数据进行流式处理的操作。
（2）java中对上述操作抽象为InputStream/OutputStream；
（3）磁盘数据IO：操作系统的IO操作依赖于文件描述符；包括所谓的网络IO，因为Socket连接也是一个打开的文件描述符；所以应用程序基于磁盘的IO操作可以解释为  
基于文件的IO操作，java抽象为FileInputStream/FileOutputStream，可以将文件数据转换为二进制流。
（4）内存数据的操作：抽象了ByteArrayInputStream/ByteArrayOutputStream作为操作内存中数据的实现，既
 计算机存储的数据，由两个地方，分别是磁盘和内存，java针对这两个地方分别实现了：；
 
 **ByteArrayInputStream作用：我们知道流中的数据是流动变化的，流本身不能复制；所以当流中的数据需要多次使用时，就需要将流中的数据拿出来**  
 **放到内存中缓存起来；在内存中使用byte[]数组保存；我们的接口之前接收的参数是InputStream，现在流没了只剩下一个byte[]数组，为了适配**  
 **保证以前接收流的地方能正常使用，需要将byte[]数组转换为流的格式，这就是ByteArrayInputStream/ByteArrayOutputStream的作用和意义**  
 **（1）能保证以前用stream的地方可以继续使用；（2）内存中的数据读写肯定比文件快；（3）当数据较大的时候不适用**  
 
 
 其它的分类由三种
 1、继承FilterInputStream类，使用装饰器模式修改，装饰了流的功能的，比如缓冲
 2、将流作为参数，进行其它操作的，比如ObjectInputStram 将二进制流转为对象；
 3、最后就是将二进制流进行编码、解码后进行操作的Reader、Writer字符流。
 
# Randomaccessfile
 前面我们所说的对文件数据的操作都是通过二进制流实现的，如果我们要修改文件中第十行的数据，我们需要获取整个文件的二进制流，  
 修改后进行输出，而randomaccessfile调用了很多本地方法从而实现直接操作文件；对于刚刚修改的第十行数据，randomaccessfile可以直接进行修改  
 而不需要获取流。

# 网络IO
## 客户端代码，都使用BIO
[客户端示例代码](../../jdk-demo/src/main/java/com/up/jdk/io/SocketClientDemo.java)
```
public  void startBioClient() throws Exception{
        Socket socket = new Socket();
        socket.setSoTimeout(SOCKET_TIMEOUT);
        socket.connect(new InetSocketAddress("127.0.0.1", 9999), CONNECT_TIMEOUT);
        // 客户端请求数据
        OutputStream os = socket.getOutputStream();
        os.write("request msg444 1111 2222 6666".getBytes());
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
```

## BIO 服务端示例
[服务端示例代码](../../jdk-demo/src/main/java/com/up/jdk/io/SocketServerDemo.java)
```
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
```

## NIO 服务端示例
[NIO服务端示例代码](../../jdk-demo/src/main/java/com/up/jdk/io/SocketServerDemo.java)
```
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
                    // 如果没有读取到结束的标记（-1）则一直从流中读取数据
                    while((length= socketChannel.read(requestBuffer)) != -1){
                        byte[] cur = new byte[length];
                        //【重要】read()方法对于requestBuffer来说是写入，对应的position位置会变化，
                        // 我们调用get去读取数据时，需要调一次flip()，将写模式转为读模式
                        requestBuffer.flip();
                        requestBuffer.get(cur, 0, length);
                        String request = new String(cur, Charset.defaultCharset());
                        System.out.println(request);
                        requestBuffer.clear(); // 读取完后将读模式转为写模式，重复使用requestBuffer
                    }
                    // 注册可写事件
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }else if(selectKey.isValid() && selectKey.isWritable()){
                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    SocketChannel socketChannel = (SocketChannel)selectKey.channel();
                    buffer.put("server response is size long out off".getBytes());
                    // socketChannel.write对于buffer来说是调用buffer的读取方法，所以需要调一次flip
                    buffer.flip();
                    socketChannel.write(buffer);
                    if (!buffer.hasRemaining()) {
                        System.out.println("   server send response successes");
                    }
                    // 发送-1标记
                    socketChannel.shutdownOutput();
                    socketChannel.close();
                }
            }
        }
    }
```
