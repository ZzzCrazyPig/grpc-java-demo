package com.crazypig.demo.grpc;

import java.util.concurrent.TimeUnit;

import com.crazypig.demo.grpc.service.HelloDeadlineRequest;
import com.crazypig.demo.grpc.service.HelloRequest;
import com.crazypig.demo.grpc.service.HelloResponse;
import com.crazypig.demo.grpc.service.HelloServiceGrpc;
import com.crazypig.demo.grpc.service.HelloServiceGrpc.HelloServiceBlockingStub;
import com.google.common.base.Verify;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;

public class HelloClient {
	
	private ManagedChannel channel;
	private HelloServiceBlockingStub blockingStub;
	
	public HelloClient(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
		blockingStub = HelloServiceGrpc.newBlockingStub(channel);
	}
	
	public HelloResponse sayHello() {
		HelloRequest request = HelloRequest.newBuilder().setName("CrazyPig").build();
		HelloResponse response = blockingStub.sayHello(request);
		return response;
	}
	
	public HelloResponse sayHelloDeadline() {
		HelloDeadlineRequest request = HelloDeadlineRequest.newBuilder().setClientDeadlineTimeInSeconds(3)
				.setName("CrazyPig").build();
		// set service call deadline
		HelloResponse response = blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS).sayHelloDeadline(request);
		return response;
	}
	
	public void stop() {
		channel.shutdown();
	}
	
	public static void main(String[] args) {
		
		HelloClient client = new HelloClient("127.0.0.1", 8888);
		try {
			client.sayHello();
		} catch (Throwable e) {
			e.printStackTrace();
			Status status = Status.fromThrowable(e);
			Verify.verify(status.getCode() == Status.Code.UNKNOWN);
		}
		
		try {
			client.sayHelloDeadline();
		} catch (Throwable t) {
			t.printStackTrace();
			Status status = Status.fromThrowable(t);
			Verify.verify(status.getCode() == Status.Code.DEADLINE_EXCEEDED);
		} finally {
			client.stop();
		}
		
	}

}
