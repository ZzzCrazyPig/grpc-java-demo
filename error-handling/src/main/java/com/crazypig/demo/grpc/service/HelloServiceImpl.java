package com.crazypig.demo.grpc.service;

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
	
	private void throwException() throws Exception {
		throw new Exception("exception");
	}
	
	

}
