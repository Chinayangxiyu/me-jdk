# 一、JUC简介

JUC（Java util concurrent），Java并发工具包。
JUC的核心：AQS、volatile、CAS、线程中断、LockSupport  
[结构脑图](./resources/JUC思维导图.png)。

## 基础知识

线程中断：线程中断是调用本地方法给线程一个标记，具体中断时间由系统判定；  
static interrupted：该方法会返回**当前线程**是否中断并**重置中断状态**；
isInterrupted：判定线程是否中断，不会重置状态； interrupt：调用线程中断。
java.util.concurrent.locks.AbstractQueuedLongSynchronizer.Node：JUC底层实现通用的队列。


# 二、locks

## 2.1 Lock、ReadWriteLock

Lock：定义的锁接口，对比synchronized更灵活；能可中断的获取锁、超时获取锁。
ReadWriteLock：定义获取读锁、写锁的接口；读写锁也是Lock。

## 2.2 Condition

概述：提供等待、唤醒线程的功能；和Object.wait()和notify()功能一样；依赖于Lock接口。
原理：Condition使用Node维护等待的线程任务队列，AQS维护的是同步队列；这种等待机制和synchronize
类似  
[流程图](/3-extend/3-3并发/resources/monitor流程图.png)。  
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/ConditionDemo.java#L8-L29)


## 2.3 LockSupport

概述：提供阻塞、解除阻塞的方法，支持中断响应和超时；
实现原理：park和unpark是用的Posix线程库pthread中的mutex（互斥量），condition（条件变量）实现的；  
mutex和condition保护了一个叫_counter的信号量。调用park时_counter置为0；调用unpark时，_counter值为1。  
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/LockSupportDemo.java#L6-L61)

## 2.4 ReentrantLock

概述：可重入的锁，实现了公平、非公平； 可重入：state值标记重入次数；  
公平锁：释放后会唤醒等待队列的第一个节点去获取资源，严格按照FIFO队列处理。  
非公平锁：锁释放后，新线程获取锁只需要替换state值成功就能获取，而同步队列中的线程需要经过唤醒等操作才能  
获取到锁，但是新线程更快；等同步队列的节点唤醒后可能state资源已经被新线程抢到了。唤醒的节点没抢到资源又  
会恢复到阻塞状态。

原理：公平锁获取时会调用hasQueuedPredecessors()方法判断同步队列是否为空，若不为空则将当前任务封装添加  
到队列；如果同步队列为空则尝试CAS获取锁。而非公平锁不会调用hasQueuedPredecessors()方法判断，直接尝试CAS  
回去state资源。

分析：公平锁因为严格按照FIFO队列处理，每次处理都需要切换上下文；非公平锁减少切换次数、提升了吞吐量，但是可能  
产生线程饥饿问题（同步队列中的线程长期获取不到锁）。  
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/ReentrantLockDemo.java)


## 2.5  ReentrantReadWriteLock

概述：可重入读写锁，同一线程可以同时获取读、写锁；多线程间，读-读锁共享的，读-写、写-写锁互斥的。  
重入原理：state是int类型的值，一共由32位；高16位表示读锁，低16位表示写；高位的值表示读锁重入次数，低位的值  
表示写锁重入次数。  
读锁：获取读锁时如果state是否被其它线程标记为写锁，或者读锁的获取次数超限，任务会被添加到队列；否则尝试CAS修改state值。  
写锁：获取写锁时如果state被其它线程标记为写锁或读锁，当前线程添加到同步队列中。  
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/ReentrantReadWriteLockDemo.java)

### 读写锁补充
（1）ReentrantReadWriteLock支持公平、非公平；设置公平锁后，获取锁时会判断同步队列是否为空，  
公平的读写锁会降级为普通的ReentrantLock，既不能实现并发读；设置非公平锁后，写锁获取时直接尝试  
CAS，读锁获取时候会判断队列头节点是否为"共享"模式。



## 2.6 AbstractQueuedSynchronizer

概述：AQS（AbstractQueueSynchronizer）队列同步器，是JUC底层实现锁、CountDownLatch等  
同步工具实现的基础；内部维护一个volatile修饰的int变量、一个FIFO的队列作为基础架构，配合  
CAS操作、LockSupport的park和unpark操作实现了一个基础的同步队列框架。

### 属性说明

**state属性**：实现独占锁时state默认锁0，获取锁后锁1；实现共享锁时state值默认锁0，获取一次加1  
释放一次减1；实现CountDownLatch等工具时，state锁对应的计数器。  
**Node(head)队列**：实现独占锁时未能获取到锁时，根据当前线程创建一个Node添加到队列尾部（tail）；实现  
共享锁时，当共享锁获取次数满了后，会将当前任务创建一个Node封装到队列中。
**nextWaiter**：表示任务链表中节点的模式，是独占模式每次只能有一个节点在处理，阻塞的节点需要被唤醒；
而共享模式的节点，没有阻塞，一直往后传递。

### 重要方法

tryAcquire：尝试以独占模式获取，既state资源只有一个；  
tryRelease：释放独占资源；  
tryAcquireShared：尝试获取共享资源，既state资源有多个；  
tryReleaseShared：尝试释放共享资源；  
acquire：独占模式获取，失败的话将当前线程封装为Node添加到队列，直到被唤醒；  
acquireInterruptibly：独占模式获取，失败的话将当下线程封装为Node添加到队列，和acquire不同的是  
本方法可以响应中断；线程在等待获取资源期间，被其它线程调用中断方法后会抛出中断异常；而acquire方法  
中的线程任务再被其它线程调用中断后，会重置中断状态。  
tryAcquireNanos：独占模式获取，失败后任务入队列，设置等待超时时间（LockSupport.parkNanos）如果  
超时未返回返回false，获取失败；如果被中断会抛出中断异常。

acquireShared：共享模式获取，获取失败线程加入队列，直到获取成功；  
acquireSharedInterruptibly：共享模式获取，获取失败加入队列；直到获取成功，或线程被中断，抛出异常；  
tryAcquireSharedNanos：共享模式获取，获取失败加入队列，直到获取成功，或失败，或线程中断抛异常；

release：释放独占资源，并唤醒队列的后继节点；  
releaseShared：释放共享资源，并唤醒当前节点的后继节点。

#### AQS总结

总结：在不同的实现中state属性含义不同。

# 三、tools

## 3.1 CountDownLatch

概述：允许一个或多个线程等待，直到其它线程执行完对应的操作（一起结束）；等待线程可以响应中断、设置超时时间。  
原理：state为CountDownLatch的容量，每调用一次countDown()计数会减1；直到为0时，唤醒等待的线程。  
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/CountDownLatchDemo.java)

## 3.2 CyclicBarrier

概述：等到一组线程全部抵达屏障拦截处时，屏障拦截才会放行（一起开始）。  
原理：state指定线程组的数量，每一个线程抵达屏障时state减1；state为0时使用Condition.signalAll()  
唤醒所有的线程。
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/CyclicBarrierDemo.java)
补充：CyclicBarrier的计数器可以重置，所以可以复用；重置时如果屏障前还有线程在等待会抛BrokenBarrierException。

## 3.3 Semaphore

概述：信号量，用于限定访问特定资源的线程数量（限流），如果当前没有资源则添加到队列中等待。  
原理：state计数；线程获取许可证时state减1，线程使用结束后归还许可证state加1，并尝试唤醒等待的任务去重新
获取许可证。
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/SemaphoreDemo.java)

## 3.4 Exchanger

概述：提供线程间交换数据的方法。
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/ExchangerDemo.java)


# 四、collections

线程安全的集合

## 4.1 ConcurrentLinkedDeque(队列)
无界双端队列，使用CAS包装线程安全；

## 4.2 BlockingQueue（阻塞队列）
阻塞队列，支持阻塞的插入和移除方法，队列满时阻塞插入线程，队列为空时阻塞移除线程。

| 队列为空或队列已满时，方法返回的结果 | 抛异常  | 返回true/false | 一直阻塞 | 超时退出             |
|:--------------------------------|:-------|:--------------|:--------|:---------------------|
| 添加                            | add(e)    | offer(e)    | put(e)  | offer（e,time,unit） |
| 移除                            | remove(e) | poll()      | take    | poll(time,unit)     |

ArrayBlockingQueue：数组结构的，有界的阻塞队列；内部使用ReentrantLock维护公平、非公平策略  。
DelayQueue：优先级队列PriorityQueue，无界阻塞；保存Delayed元素；队列头保存延迟时间最长或最短  
的元素（取决于Delayed的实现），当获取元素时延迟时间未到，则阻塞。  
LinkedBlockingQueue：链表，有界阻塞。  
PriorityBlockingQueue：优先级队列PriorityQueue，无界阻塞。  
SynchronousQueue：没有容量的阻塞队列，每次插入都必须等待另一个线程的删除操作，  
LinkedTransferQueue：链表结构，无界阻塞，提供transfer方法，当消费者没有消费则一直阻塞，直到消费者消费后唤醒。


## 4.3  LinkedBlockingDeque（阻塞双端队列）
链表结构的双向阻塞队列。

## 4.4 CopyOnWriteArrayList
线程安全的ArrayList，维护volatile修饰的数组，保证读的是最新的，使用ReentrantLock保证写的安全性。

## 4.5 CopyOnWriteArraySet
线程安全的Set，使用数组存储元素，效率很低，新增、删除操作都会copy一次数据。

## 4.5 ConcurrentSkipListSet
线程安全的有序集合，类似TreeSet；底层由ConcurrentSkipListMap实现。

## 4.6 ConcurrentMap
ConcurrentSkipListMap：底层使用跳表实现，有序的，线程安全的Map。  
ConcurrentHashMap：底层使用数据、链表红黑树结构；使用CAS、synchronized保证线程安全。


# 五、线程池
## 5.1 Future
异步获取线程计算结果，计算未完成阻塞线程；也可以取消线程执行。
ScheduledFuture：在Future接口的基础上扩展了Delayed接口，为执行定时、延迟任务的线程池提供的任务接口。
FutureTask：异步计算的实现，每一个FutureTask对象就是一个Runnable任务，当其它线程调用实例的get方法时；
如果Runnable执行没结束，则调用get的线程会被阻塞，知道run方法执行结束后去唤醒。

## 5.2 Callable
异步计算的时候可以获取计算结果，抛出异常。比如在FutureTask就在Runnable的基础上装饰增强了
拥有返回值的功能。

## 5.3 线程池
ExecutorService：线程池接口。
ThreadPoolExecutor：线程池实现类，包括核心线程数、任务队列、最大线程数、空闲线程存活时间、拒绝策略等参数。


ScheduledExecutorService：延迟或定期执行的线程池接口。
ScheduledThreadPoolExecutor：延迟、定期执行任务的线程池，在ThreadPoolExecutor的基础上扩展了定时、延期执行任务的功能。
[示例](../../jdk-demo/src/main/java/com/up/jdk/juc/ExecutorDemo.java)

RejectedExecutionHandler：拒绝策略的接口，默认有四个，可以自定义实现。
Executors：创建线程池的工厂类。

## 5.4 CompletableFuture
jdk1.8对Future的扩展，增强了异步回调、流式处理、多个Future组合处理的能力；使得Java多任务的协调处理更加流畅。  
描述：一般使用CompletableFuture的静态方法获取实例，

### （1）提交任务的方法（静态方法）
CompletableFuture.runAsync(Runnable runnable):无返回值任务提交，使用默认线程池ForkJoinPool。  
CompletableFuture.runAsync(Runnable runnabl, Executor executor):无返回值任务提交，使用传入的线程池执行。  
CompletableFuture.supplyAsync(Supplier<U> supplier):有返回值任务提交，使用默认线程池ForkJoinPool。  
CompletableFuture.supplyAsync(Supplier<U> supplier, Executor executor):有返回值任务提交，使用传入的线程池。

### （2）实例方法举例（异步回调、流式处理）
（1）异步回调：相比Future的异步回调开发更简单易懂
```
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return " execute supply";
        });
        // 将future的返回结果作为参数传入thenApply进行异步回调处理。
        future.thenApply((param)->{

            try {
                Thread.sleep(5000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("thenApply" + param);
            return "aa:" + param;
        });
        
        System.out.println("main is ending");

```
（2）流式处理:如果用Future进行流式处理，代码实现太复杂
```
 CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return " execute supply";
        });

        future.thenApply((result) ->{
            System.out.println(result + " thenApply");
            return result + " thenApply";
        })
                .thenAccept((result) -> {
                    System.out.println(result + " thenAccept");
                })
                .thenRun(() -> {
                    System.out.println("thenRun");

                });

        System.out.println("pipeline ending");
```


(参考1)\[https://blog.csdn.net/qq_31865983/article/details/106137777]  
(参考2)\[https://www.jianshu.com/p/558b090ae4bb?from=timeline]

### （3）操作方法

# 四、atomic

对于volatile修饰的变量，提供线程安全的原子操作。
AtomicIntegerFieldUpdater：基于反射，提供了volatile修饰的Integer类型属性的原子操作；
AtomicLongFieldUpdater：基于反射，提供了volatile修饰的Long类型属性的原子操作；
AtomicReference：更新引用的原子操作；
AtomicReferenceFieldUpdater：基于反射，提供了volatile修饰的Reference类型属性的原子操作。


# 面试问题

## 1、阻塞队列实现原理
（1）阻塞的插入和移除为阻塞队列，实现原理是通过Condition实现线程间的通信来实现的。  
（2）而Condition实现的线程间通信是基于LockSupport.park()、unpark()实现的；  
（3）底层通过调用Unsafe的本地方法完成线程的等待和唤醒，Unsafe.park对应Linux的系统方法  
pthread_cond_wait，Unsafe.unpark()对应Linux的系统方法pthread_cond_signal。

## 2、定时、周期任务线程池(ScheduledThreadPoolExecutor)实现原理
（1）定时任务线程池的任务队列由DelayQueue实现，而DelayQueue内部维护的是由Delayed  
元素构成成的PriorityQueue（优先级）队列，PriorityQueue按照Delayed元素的剩余过期  
时间进行顺序排列。  
（2）因为DelayQueue是延迟队列，所以ScheduledThreadPoolExecutor从DelayQueue中获取  
任务执行时，如果任务到期时间未到；则会根据到期时间和当前时间计算出一个等待时间(nanosTimeout)，  
当前线程任务会被阻塞，直到等待时间超时。

## 3、