package org.aaron.netty2280test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNIO {

	private static final Logger log = LoggerFactory.getLogger(TestNIO.class);

	private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

	private static final HashedWheelTimer timer = new HashedWheelTimer();

	private static final String SERVER_ADDRESS = "127.0.0.1";

	private static final int SERVER_PORT = 54321;

	private static class ClientHandler extends
			SimpleChannelInboundHandler<Object> {

		public ClientHandler() {

		}

		@Override
		public void channelRegistered(ChannelHandlerContext ctx)
				throws Exception {
			log.info("channelRegistered " + ctx.channel());
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			log.info("channelActive " + ctx.channel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			log.info("exceptionCaught " + ctx.channel(), cause);
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx)
				throws Exception {
			log.info("channelUnegistered " + ctx.channel());
			reconnectAfterDelay();
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx, Object message) {
			log.info("channelRead0 " + ctx.channel() + " " + message);
		}
	}

	private static class ClientChannelInitializer extends
			ChannelInitializer<Channel> {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			final ChannelPipeline p = ch.pipeline();
			p.addLast("logger", new LoggingHandler(LogLevel.DEBUG));

			p.addLast("clientHandler", new ClientHandler());
		}
	}

	private static void reconnectAfterDelay() {
		timer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				try {
					reconnect();
				} catch (Exception e) {
					log.warn("run", e);
				}
			}
		}, 1, TimeUnit.SECONDS);
	}

	private static void reconnect() {
		final Bootstrap b = new Bootstrap();
		b.group(eventLoopGroup).channel(NioSocketChannel.class)
				.handler(new ClientChannelInitializer())
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
		b.connect(SERVER_ADDRESS, SERVER_PORT);
	}

	public static void main(String[] args) {
		reconnect();
	}

}
