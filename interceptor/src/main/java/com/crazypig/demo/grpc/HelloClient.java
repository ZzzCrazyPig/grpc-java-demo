package com.crazypig.demo.grpc;

import com.crazypig.demo.grpc.interceptor.HelloClientInterceptor;
import com.crazypig.demo.grpc.service.HelloDeadlineRequest;
import com.crazypig.demo.grpc.service.HelloRequest;
import com.crazypig.demo.grpc.service.HelloResponse;
import com.crazypig.demo.grpc.service.HelloServiceGrpc;
import com.crazypig.demo.grpc.service.HelloServiceGrpc.HelloServiceBlockingStub;

import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloClient {
	
	private ManagedChannel channel;
	private HelloServiceBlockingStub blockingStub;
	
	public HelloClient(String host, int port, boolean illegal) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
		if (illegal) {
			blockingStub = HelloServiceGrpc.newBlockingStub(channel);
		} else {
			blockingStub = HelloServiceGrpc.newBlockingStub(ClientInterceptors.intercept(channel, new HelloClientInterceptor()));
		}
	}
	
	public HelloResponse sayHello() {
		HelloRequest request = HelloRequest.newBuilder().setName("CrazyPig").build();
		HelloResponse response = blockingStub.sayHello(request);
		return response;
	}
	
	public HelloResponse sayHelloDeadline() {
		HelloDeadlineRequest request = HelloDeadlineRequest.newBuilder().setName("CrazyPig").setDeadlineInSeconds(3).build();
		HelloResponse response = blockingStub.sayHelloDeadline(request);
		return response;
	}
	
	public void stop() {
		channel.shutdown();
	}
	
	public static void main(String[] args) {
		
		HelloClient client = new HelloClient("127.0.0.1", 8888, false);
		sayHelloAndThenStop(client);
		
		HelloClient illegalClient = new HelloClient("127.0.0.1", 8888, true);
		sayHelloAndThenStop(illegalClient);
		
		HelloClient deadlineClient = new HelloClient("127.0.0.1", 8888, false);
		sayHelloDeadlineAndThenStop(deadlineClient);
		
	}
	
	private static void sayHelloAndThenStop(HelloClient client) {
		try {
			client.sayHello();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.stop();
			}
		}
	}
	
	private static void sayHelloDeadlineAndThenStop(HelloClient client) {
		try {
			client.sayHelloDeadline();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.stop();
			}
		}
	}

}
