package com.crazypig.demo.grpc.hw;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.crazypig.demo.grpc.hw.rpc.HelloRequest;
import com.crazypig.demo.grpc.hw.rpc.HelloResponse;
import com.crazypig.demo.grpc.hw.rpc.HelloServiceGrpc;
import com.crazypig.demo.grpc.hw.rpc.HelloServiceGrpc.HelloServiceBlockingStub;
import com.crazypig.demo.grpc.hw.rpc.HelloServiceGrpc.HelloServiceFutureStub;
import com.crazypig.demo.grpc.hw.rpc.HelloServiceGrpc.HelloServiceStub;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class HelloClient {
	
	private static HelloResponse ERR_RESPONSE = HelloResponse.newBuilder().setMessage("ERROR").build();
	
	private ManagedChannel channel;
	private HelloServiceBlockingStub blockingStub;
	private HelloServiceFutureStub futureStub;
	private HelloServiceStub stub;
	
	private HelloClient(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
		blockingStub = HelloServiceGrpc.newBlockingStub(channel);
		futureStub = HelloServiceGrpc.newFutureStub(channel);
		stub = HelloServiceGrpc.newStub(channel);
	}
	
	public static HelloClient build(String host, int port) {
		return new HelloClient(host, port);
	}
	
	public HelloResponse sayHelloUsingBlockingStub(HelloRequest request) {
		// equals to futureStub.sayHello(request).get()
		// futureStub provide client side async processing of the server response
		return blockingStub.sayHello(request);
	}
	
	public HelloResponse sayHelloUsingFutureStub(HelloRequest request) {
		ListenableFuture<HelloResponse> future = futureStub.sayHello(request);
		HelloResponse response = null;
		try {
			response = future.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			response = ERR_RESPONSE;
		}
		return response;
	}
	
	public HelloResponse sayHelloUsingAsyncStub(HelloRequest request) {
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<HelloResponse> result = Lists.newArrayList();
		StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {

			@Override
			public void onNext(HelloResponse value) {
				result.add(value);
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
				result.add(ERR_RESPONSE);
				cdl.countDown();
			}

			@Override
			public void onCompleted() {
				cdl.countDown();
			}
		};
		stub.sayHello(request, responseObserver);
		try {
			cdl.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result.get(0);
	}
	
	/**
	 * server-side stream call, using blocking stub
	 * @param request
	 * @return
	 */
	public List<HelloResponse> sayServerSideStreamHelloUsingBlockingStub(HelloRequest request) {
		return Lists.newArrayList(blockingStub.sayServerSideStreamHello(request));
	}
	
	/**
	 * server-side stream call, using async stub
	 * @param request
	 * @return
	 */
	public List<HelloResponse> sayServerSideStreamHelloUsingAsyncStub(HelloRequest request) {
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<HelloResponse> result = Lists.newArrayList();
		// client-side streaming processing
		StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {

			@Override
			public void onNext(HelloResponse value) {
				result.add(value);
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
				cdl.countDown();
			}

			@Override
			public void onCompleted() {
				cdl.countDown();
			}
		};
		// blockingStub.sayServerSideStreamHello(request) block until all the stream responses are received!
		stub.sayServerSideStreamHello(request, responseObserver);
		try {
			cdl.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * client-side stream call
	 * @return
	 */
	public List<HelloResponse> sayClientSideStreamHello(Collection<HelloRequest> requests) {
		
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<HelloResponse> responses = Lists.newArrayList();
		StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {

			@Override
			public void onCompleted() {
				System.out.println("sayClientSideStreamHello onCompleted");
				cdl.countDown();
			}

			@Override
			public void onError(Throwable error) {
				System.out.println("sayClientSideStreamHello onError");
				error.printStackTrace();
				cdl.countDown();
			}

			@Override
			public void onNext(HelloResponse response) {
				System.out.println("sayClientSideStreamHello onNext");
				responses.add(response);
			}
		};
		
		StreamObserver<HelloRequest> requestStreamObserver = stub.sayClientSideStreamHello(responseObserver);
		for (HelloRequest request : requests) {
			requestStreamObserver.onNext(request);
		}
		requestStreamObserver.onCompleted();
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return responses;
	}
	
	/**
	 *  both-side stream call
	 * @param requests
	 * @return
	 */
	public List<HelloResponse> sayBothSideStreamHello(Collection<HelloRequest> requests) {
		final CountDownLatch cdl = new CountDownLatch(1);
		final List<HelloResponse> responses = Lists.newArrayList();
		StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {

			@Override
			public void onNext(HelloResponse response) {
				responses.add(response);
			}

			@Override
			public void onError(Throwable t) {
				cdl.countDown();
			}

			@Override
			public void onCompleted() {
				cdl.countDown();
			}
		};
				
		StreamObserver<HelloRequest> requestObserver = stub.sayBothSideStreamHello(responseObserver);
		for (HelloRequest request : requests) {
			requestObserver.onNext(request);
		}
		requestObserver.onCompleted();
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return responses;
	}
	
	public void close() {
		try {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		HelloClient client = new HelloClient("localhost", 8888);
		
		HelloRequest singleRequest = HelloRequest.newBuilder().setName("CrazyPig").build();
		List<HelloRequest> multiRequests = Lists.newArrayList();
		multiRequests.add(HelloRequest.newBuilder().setName("CrazyPig01").build());
		multiRequests.add(HelloRequest.newBuilder().setName("CrazyPig02").build());
		multiRequests.add(HelloRequest.newBuilder().setName("CrazyPig03").build());
		
		HelloResponse response = client.sayHelloUsingBlockingStub(singleRequest);
		System.out.println("[sayHelloUsingBlockingStub] client get response from server : " + response.getMessage());
		
		response = client.sayHelloUsingAsyncStub(singleRequest);
		System.out.println("[sayHelloUsingAsyncStub] client get response from server : " + response.getMessage());
		
		response = client.sayHelloUsingFutureStub(singleRequest);
		System.out.println("[sayHelloUsingFutureStub] client get response from server : " + response.getMessage());
		
		List<HelloResponse> responses = client.sayServerSideStreamHelloUsingBlockingStub(singleRequest);
		System.out.println("[sayServerSideStreamHelloUsingBlockingStub] client get responses from server : { ");
		for (HelloResponse r : responses) {
			System.out.println(r.getMessage());
		}
		System.out.println("}\r\n");
		
		responses = client.sayServerSideStreamHelloUsingAsyncStub(singleRequest);
		System.out.println("[sayServerSideStreamHelloUsingAsyncStub] client get responses from server : { ");
		for (HelloResponse r : responses) {
			System.out.println(r.getMessage());
		}
		System.out.println("}\r\n");
		
		responses = client.sayClientSideStreamHello(multiRequests);
		System.out.println("[sayClientSideStreamHello] client get responses from server : { ");
		for (HelloResponse r : responses) {
			System.out.println(r.getMessage());
		}
		System.out.println("}\r\n");
		
		responses = client.sayBothSideStreamHello(multiRequests);
		System.out.println("[sayBothSideStreamHello] client get responses from server : { ");
		for (HelloResponse r : responses) {
			System.out.println(r.getMessage());
		}
		System.out.println("}\r\n");
		
		// need to close client finally
		client.close();
	}
	
}
