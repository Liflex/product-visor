package ru.dmitartur.common.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerAuthInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String auth = headers.get(AUTHORIZATION);
        if (auth == null || !auth.toLowerCase().startsWith("bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing Bearer token"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        // Здесь можно добавить полноценную валидацию JWT при необходимости
        return next.startCall(call, headers);
    }
}
