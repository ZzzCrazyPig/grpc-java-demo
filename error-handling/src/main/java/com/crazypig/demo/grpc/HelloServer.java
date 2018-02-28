package com.crazypig.demo.grpc;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.crazypig.demo.grpc.service.HelloServiceImpl;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class HelloServer {
	
	private int port;
	private Server gRpcServer;
	
	public HelloServer(int port) {
		this.port = port;
	}
	
	public void start() throws IOException {
		gRpcServer = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", port))
				.addService(new HelloServiceImpl()).build();
		gRpcServer.start();
	}
	
	public void await() throws InterruptedException {
		if (gRpcServer != null) {
			gRpcServer.awaitTermination();
		}
	}
	
	public static void main(String[] args) {
		HelloServer server = new HelloServer(8888);
		try {
			server.start();
			System.out.println("server start!");
			server.await();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
