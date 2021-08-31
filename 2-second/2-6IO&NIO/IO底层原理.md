# 一、IO基础原理
## 为什么要有缓冲
操作系统的read、write是系统调用，会导致系统中断，所以需要将数据攒起来，然后再进行系统调用避免频繁中断。
## 四种IO模型
### BIO（同步阻塞IO）
调用read后，需要等待数据从网卡 -> 系统内存 -> 应用内存。
优点：开发简单
缺点：每个连接消耗一个线程资源，高并发的应用场景下，需要大量的线程来维护大量的网络连接，
内存、线程切换开销会非常巨大

``` 
 //客户端
 public  void startSimpleClient() throws Exception{
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 9000));
        OutputStream os = socket.getOutputStream();
        os.write("request msg444".getBytes());

        InputStream is = socket.getInputStream();
        byte[] response = new byte[64];
        is.read(response);

        System.out.println("======== [response] ======== ");
        System.out.println(new String(response));
        socket.close();
    }
    
 // 服务端
 public void simpleServer() throws Exception{
        ServerSocket server = new ServerSocket(9000);
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

### Non booking IO（同步非阻塞IO）
调用read后，一直去轮询数据状态，
不实用，轮询会占用大量CPU时间，导致CPU效率降低。

### IO多路复用
调用read操作后，将socket注册到系统的，然后当前线程（thread1）执行其它的任务，操作系统的select/epoll
事件会监听多个socket，当对应的事件（可读、可写）准备就绪后，会返回给调用的线程thread1。
优点：节省了线程资源（不用每个connect创建线程），减少系统开销。
