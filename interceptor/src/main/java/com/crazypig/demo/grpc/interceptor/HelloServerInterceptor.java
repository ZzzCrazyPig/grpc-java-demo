package com.crazypig.demo.grpc.interceptor;

import java.util.UUID;

import com.google.common.base.Strings;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class HelloServerInterceptor implements ServerInterceptor {

	private static final Key<String> SERVER_TOKEN = Metadata.Key.of("server_token", Metadata.ASCII_STRING_MARSHALLER);
	private static final Key<String> CLIENT_TOKEN = Metadata.Key.of("client_token", Metadata.ASCII_STRING_MARSHALLER);
	
	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		String clientToken = headers.get(CLIENT_TOKEN);
		// illegal client token, close the server call with special status
		if (isClientTokenIllegal(clientToken)) {
			call.close(Status.ABORTED.withDescription("illegal CLIENT_TOKEN"), headers);
			return new Listener<ReqT>() {};
		}
		System.out.println("receive client token : " + String.valueOf(clientToken));
		return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {

			@Override
			public void sendHeaders(Metadata headers) {
				headers.put(SERVER_TOKEN, UUID.randomUUID().toString());
				super.sendHeaders(headers);
			}
			
		}, headers);
	}
	
	private boolean isClientTokenIllegal(String clientToken) {
		return Strings.isNullOrEmpty(clientToken);
	}

}
