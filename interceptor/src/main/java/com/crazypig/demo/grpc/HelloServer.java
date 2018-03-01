package com.crazypig.demo.grpc;

import java.io.IOException;

import com.crazypig.demo.grpc.interceptor.HelloServerInterceptor;
import com.crazypig.demo.grpc.service.HelloServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

public class HelloServer {
	
	private int port;
	private Server gRpcServer;
	
	public HelloServer(int port) {
		this.port = port;
	}
	
	public void start() throws IOException {
		gRpcServer = ServerBuilder.forPort(port)
				.addService(ServerInterceptors.intercept(new HelloServiceImpl(), new HelloServerInterceptor())).build();
		gRpcServer.start();
	}
	
	public void awaitTermination() throws InterruptedException {
		gRpcServer.awaitTermination();
	}
	
	public void stop() throws InterruptedException {
		gRpcServer.shutdown();
	}
	
	public static void main(String[] args) {
		HelloServer helloServer = new HelloServer(8888);
		try {
			helloServer.start();
			System.out.println("server start!");
			helloServer.awaitTermination();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
