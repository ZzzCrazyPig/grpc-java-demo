package com.crazypig.demo.grpc.hw.service;

import com.crazypig.demo.grpc.hw.rpc.HelloRequest;
import com.crazypig.demo.grpc.hw.rpc.HelloResponse;
import com.crazypig.demo.grpc.hw.rpc.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceImplBase {

	@Override
	public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		HelloResponse response = HelloResponse.newBuilder().setMessage(request.getName()).build();
		responseObserver.onNext(response);
		// WARN: return multiple response will throws exception
		// io.grpc.StatusRuntimeException: INTERNAL: Too many responses
		// responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<HelloRequest> sayClientSideStreamHello(final StreamObserver<HelloResponse> responseObserver) {
		// WARN: return multiple response will throws exception
		// io.grpc.StatusRuntimeException: INTERNAL: Too many responses
		StreamObserver<HelloRequest> requestObserver = new StreamObserver<HelloRequest>() {

			@Override
			public void onCompleted() {
				responseObserver.onNext(HelloResponse.newBuilder().setMessage("Server Response : Only One").build());
				responseObserver.onCompleted();
			}

			@Override
			public void onError(Throwable error) {
				responseObserver.onError(error);
			}

			@Override
			public void onNext(HelloRequest request) {
//				responseObserver.onNext(HelloResponse.newBuilder().setMessage("Server Response : " + request.getName()).build());
			}
		};
		return requestObserver;
	}

	@Override
	public void sayServerSideStreamHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		responseObserver.onNext(HelloResponse.newBuilder().setMessage("Server Response : " + request.getName()).build());
		responseObserver.onNext(HelloResponse.newBuilder().setMessage("Server Response : " + request.getName()).build());
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<HelloRequest> sayBothSideStreamHello(final StreamObserver<HelloResponse> responseObserver) {
		
		StreamObserver<HelloRequest> requestObserver = new StreamObserver<HelloRequest>() {

			@Override
			public void onNext(HelloRequest request) {
				HelloResponse response = HelloResponse.newBuilder()
						.setMessage("Server Response : " + request.getName()).build();
				responseObserver.onNext(response);
			}

			@Override
			public void onError(Throwable t) {
				responseObserver.onError(t);
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}
		};
		
		return requestObserver;
	}

	
}
