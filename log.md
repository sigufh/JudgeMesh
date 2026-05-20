
D:\JudgeMesh>Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:46:56.871Z  INFO 1 --- [user-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [bc27b8ae-2ca7-46a3-89a6-d8846bd13b43_config-0] Fail to connect server, after trying 25 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:46:59.483Z  INFO 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:46:59.577Z ERROR 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:46:59.595Z  INFO 1 --- [user-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [bc27b8ae-2ca7-46a3-89a6-d8846bd13b43_config-0] Fail to connect server, after trying 26 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:02.309Z  INFO 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:02.389Z ERROR 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:02.419Z  INFO 1 --- [user-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [bc27b8ae-2ca7-46a3-89a6-d8846bd13b43_config-0] Fail to connect server, after trying 27 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:04.062Z  INFO 1 --- [user-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8081 (http)
2026-05-19T22:47:05.249Z  INFO 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:05.342Z ERROR 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:05.371Z  INFO 1 --- [user-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [bc27b8ae-2ca7-46a3-89a6-d8846bd13b43_config-0] Fail to connect server, after trying 28 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:05.383Z  INFO 1 --- [user-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 41784 ms
2026-05-19T22:47:08.287Z  INFO 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:08.380Z ERROR 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:08.383Z  INFO 1 --- [user-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [bc27b8ae-2ca7-46a3-89a6-d8846bd13b43_config-0] Fail to connect server, after trying 29 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:11.402Z  INFO 1 --- [user-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:00.094Z  INFO 1 --- [problem-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [9bcddb92-af42-4b92-8865-a0c8fead78ac_config-0] Fail to connect server, after trying 26 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:02.836Z  INFO 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:02.916Z ERROR 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:02.988Z  INFO 1 --- [problem-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [9bcddb92-af42-4b92-8865-a0c8fead78ac_config-0] Fail to connect server, after trying 27 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:04.918Z  INFO 1 --- [problem-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8082 (http)
2026-05-19T22:47:05.793Z  INFO 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:05.996Z ERROR 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:06.011Z  INFO 1 --- [problem-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [9bcddb92-af42-4b92-8865-a0c8fead78ac_config-0] Fail to connect server, after trying 28 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:06.439Z  INFO 1 --- [problem-service] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 44510 ms
2026-05-19T22:47:08.924Z  INFO 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:08.986Z ERROR 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:08.998Z  INFO 1 --- [problem-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [9bcddb92-af42-4b92-8865-a0c8fead78ac_config-0] Fail to connect server, after trying 29 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
2026-05-19T22:47:12.001Z  INFO 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : grpc client connection server: nacos.judgemesh-infra.svc ip, serverPort: 9848, grpcTslConfig: {"enableTls":false,"mutualAuthEnable":false,"trustAll":true}
2026-05-19T22:47:12.023Z ERROR 1 --- [problem-service] [remote.worker.1] c.a.n.c.remote.client.grpc.GrpcClient    : Server check fail, please check server nacos.judgemesh-infra.svc, port 9848 is available, error ={}

java.util.concurrent.ExecutionException: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:594) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:469) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.serverCheck(GrpcClient.java:234) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer(GrpcClient.java:346) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.reconnect(RpcClient.java:504) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.common.remote.client.RpcClient.lambda$start$1(RpcClient.java:330) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.alibaba.nacos.shaded.io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
	at com.alibaba.nacos.shaded.io.grpc.Status.asRuntimeException(Status.java:532) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:538) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener$3.run(DelayedClientCall.java:489) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.delayOrExecute(DelayedClientCall.java:453) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.DelayedClientCall$DelayedListener.onClose(DelayedClientCall.java:486) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:564) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl.access$100(ClientCallImpl.java:72) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:729) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:710) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:133) ~[nacos-client-3.0.3.jar:na]
	... 3 common frames omitted
Caused by: com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: nacos.judgemesh-infra.svc/34.118.228.29:9848
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:994) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[nacos-client-3.0.3.jar:na]
	at com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[nacos-client-3.0.3.jar:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-05-19T22:47:12.029Z  INFO 1 --- [problem-service] [remote.worker.1] com.alibaba.nacos.common.remote.client   : [9bcddb92-af42-4b92-8865-a0c8fead78ac_config-0] Fail to connect server, after trying 30 times, last try server is {serverIp = 'nacos.judgemesh-infra.svc', server main port = 8848}, error = unknown
22:47:32.357 [main] INFO com.alibaba.nacos.plugin.auth.spi.client.ClientAuthPluginManager -- [ClientAuthPluginManager] Load ClientAuthService com.alibaba.nacos.client.auth.impl.NacosClientAuthServiceImpl success.
22:47:32.357 [main] INFO com.alibaba.nacos.plugin.auth.spi.client.ClientAuthPluginManager -- [ClientAuthPluginManager] Load ClientAuthService com.alibaba.nacos.client.auth.ram.RamClientAuthServiceImpl success.
