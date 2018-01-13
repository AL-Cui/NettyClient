package core;

import handler.HeartBeatClientHandler;
import handler.WaterPurifierMessageHandler;
import heartbeat.ConnectorIdleStateTrigger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reconnection.ConnectionWatchdog;
import utils.Util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class HeartBeatsClientTest extends Thread {
    private static final String Url = "iotwater-rd.smart-blink.com";    //服务器域名
    public static final Logger logger = LoggerFactory.getLogger(HeartBeatsClientTest.class);
    private String macAddress;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;


    HeartBeatsClientTest(String mac,Bootstrap boot,EventLoopGroup eventLoopGroup) {
        this.macAddress = mac;
        this.bootstrap = boot;
        this.eventLoopGroup = eventLoopGroup;

    }

    protected final HashedWheelTimer timer = new HashedWheelTimer();

    private ConnectorIdleStateTrigger idleStateTrigger;

    /***
     * 客户端向服务端建立连接
     * @param port
     * @param host
     * @throws Exception
     */
    public void connect(int port, String host) throws Exception {
        idleStateTrigger = new ConnectorIdleStateTrigger(macAddress);
        logger.info(macAddress);

        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer, port, host, true, macAddress) {
            public ChannelHandler[] handlers() {
                ByteBuf delimiter = Unpooled.copiedBuffer("\r\n".getBytes());
                return new ChannelHandler[]{
                        this,
                        new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                        new DelimiterBasedFrameDecoder(1024, delimiter), //分隔符
                        new StringDecoder(),
                        new StringEncoder(),
                        new HeartBeatClientHandler(),
                        new WaterPurifierMessageHandler(macAddress),
                        idleStateTrigger

                };
            }
        };

        ChannelFuture future;
        //进行连接
        try {
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer<Channel>() {

                    //初始化channel
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        SSLContext sslContext = Util.initSSLContext();
                        SSLEngine sslEngine = sslContext.createSSLEngine();
                        sslEngine.setUseClientMode(true);
                        ch.pipeline().addFirst("ssl", new SslHandler(sslEngine));
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });

                future = bootstrap.connect(host, port);
            }

            // 以下代码在synchronized同步块外面是安全的
            future.sync();
        } catch (Throwable t) {
            throw new Exception("connects to  fails", t);
        }
    }

    @Override
    public void run() {
        super.run();
        String ipAddress;
        try {
//            ipAddress = InetAddress.getByName(Url).getHostAddress();
//            System.out.println(ipAddress);
            int port = 2001;
            connect(port, "10.191.201.13");
            //connect(port, "127.0.0.1");
//        connect(port, "10.191.200.128");
//        connect(port, ipAddress);
//        connect(port, "123.56.157.215");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
