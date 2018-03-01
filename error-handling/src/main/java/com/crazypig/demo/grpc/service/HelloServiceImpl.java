package com.crazypig.demo.grpc.service;

import java.util.concurrent.TimeUnit;

import com.crazypig.demo.grpc.service.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceImplBase {

	@Override
	public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		try {
			throwException();
		} catch (Exception e) {
			responseObserver.onError(e);
		}
	}
	
	
	@Override
	public void sayHelloDeadline(HelloDeadlineRequest request, StreamObserver<HelloResponse> responseObserver) {
		int clientDeadLineTimeInSeconds = request.getClientDeadlineTimeInSeconds();
		// sleep ...
		try {
			TimeUnit.SECONDS.sleep(clientDeadLineTimeInSeconds + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("after sleep, send response to client");
		responseObserver.onNext(HelloResponse.newBuilder().setMessage(request.getName()).build());
		responseObserver.onCompleted();
	}



	private void throwException() throws Exception {
		throw new Exception("exception");
	}
	
	

}
