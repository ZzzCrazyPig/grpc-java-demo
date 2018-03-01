package com.crazypig.demo.grpc.service;

import com.crazypig.demo.grpc.service.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceImplBase {

	@Override
	public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		System.out.println("HelloServiceImpl sayHello");
		responseObserver.onNext(HelloResponse.newBuilder().setMessage(request.getName()).build());
		responseObserver.onCompleted();
	}
	
}
