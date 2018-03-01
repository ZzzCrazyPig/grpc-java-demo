package com.crazypig.demo.grpc.interceptor;

import java.util.UUID;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;

public class HelloClientInterceptor implements ClientInterceptor {

	private static final Key<String> CLIENT_TOKEN = Metadata.Key.of("client_token", Metadata.ASCII_STRING_MARSHALLER);
	private static final Key<String> SERVER_TOKEN = Metadata.Key.of("server_token", Metadata.ASCII_STRING_MARSHALLER);
	
	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				headers.put(CLIENT_TOKEN, UUID.randomUUID().toString());
				super.start(new SimpleForwardingClientCallListener(responseListener) {

					@Override
					public void onHeaders(Metadata headers) {
						String serverToken = headers.get(SERVER_TOKEN);
						System.out.println("receive server token : " + serverToken);
					}
					
					
				}, headers);
			}
			
			
		};
	}

}
