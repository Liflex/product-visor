package ru.dmitartur.common.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: orders.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class OrderInternalServiceGrpc {

  private OrderInternalServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "productvisor.orders.v1.OrderInternalService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ru.dmitartur.common.grpc.JsonRequest,
      ru.dmitartur.common.grpc.JsonResponse> getUpsertOrdersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpsertOrders",
      requestType = ru.dmitartur.common.grpc.JsonRequest.class,
      responseType = ru.dmitartur.common.grpc.JsonResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ru.dmitartur.common.grpc.JsonRequest,
      ru.dmitartur.common.grpc.JsonResponse> getUpsertOrdersMethod() {
    io.grpc.MethodDescriptor<ru.dmitartur.common.grpc.JsonRequest, ru.dmitartur.common.grpc.JsonResponse> getUpsertOrdersMethod;
    if ((getUpsertOrdersMethod = OrderInternalServiceGrpc.getUpsertOrdersMethod) == null) {
      synchronized (OrderInternalServiceGrpc.class) {
        if ((getUpsertOrdersMethod = OrderInternalServiceGrpc.getUpsertOrdersMethod) == null) {
          OrderInternalServiceGrpc.getUpsertOrdersMethod = getUpsertOrdersMethod =
              io.grpc.MethodDescriptor.<ru.dmitartur.common.grpc.JsonRequest, ru.dmitartur.common.grpc.JsonResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpsertOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.dmitartur.common.grpc.JsonRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ru.dmitartur.common.grpc.JsonResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderInternalServiceMethodDescriptorSupplier("UpsertOrders"))
              .build();
        }
      }
    }
    return getUpsertOrdersMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OrderInternalServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceStub>() {
        @java.lang.Override
        public OrderInternalServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderInternalServiceStub(channel, callOptions);
        }
      };
    return OrderInternalServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OrderInternalServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceBlockingStub>() {
        @java.lang.Override
        public OrderInternalServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderInternalServiceBlockingStub(channel, callOptions);
        }
      };
    return OrderInternalServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OrderInternalServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderInternalServiceFutureStub>() {
        @java.lang.Override
        public OrderInternalServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderInternalServiceFutureStub(channel, callOptions);
        }
      };
    return OrderInternalServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void upsertOrders(ru.dmitartur.common.grpc.JsonRequest request,
        io.grpc.stub.StreamObserver<ru.dmitartur.common.grpc.JsonResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpsertOrdersMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service OrderInternalService.
   */
  public static abstract class OrderInternalServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return OrderInternalServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service OrderInternalService.
   */
  public static final class OrderInternalServiceStub
      extends io.grpc.stub.AbstractAsyncStub<OrderInternalServiceStub> {
    private OrderInternalServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderInternalServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderInternalServiceStub(channel, callOptions);
    }

    /**
     */
    public void upsertOrders(ru.dmitartur.common.grpc.JsonRequest request,
        io.grpc.stub.StreamObserver<ru.dmitartur.common.grpc.JsonResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpsertOrdersMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service OrderInternalService.
   */
  public static final class OrderInternalServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<OrderInternalServiceBlockingStub> {
    private OrderInternalServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderInternalServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderInternalServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public ru.dmitartur.common.grpc.JsonResponse upsertOrders(ru.dmitartur.common.grpc.JsonRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpsertOrdersMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service OrderInternalService.
   */
  public static final class OrderInternalServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<OrderInternalServiceFutureStub> {
    private OrderInternalServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderInternalServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderInternalServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ru.dmitartur.common.grpc.JsonResponse> upsertOrders(
        ru.dmitartur.common.grpc.JsonRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpsertOrdersMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPSERT_ORDERS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPSERT_ORDERS:
          serviceImpl.upsertOrders((ru.dmitartur.common.grpc.JsonRequest) request,
              (io.grpc.stub.StreamObserver<ru.dmitartur.common.grpc.JsonResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getUpsertOrdersMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              ru.dmitartur.common.grpc.JsonRequest,
              ru.dmitartur.common.grpc.JsonResponse>(
                service, METHODID_UPSERT_ORDERS)))
        .build();
  }

  private static abstract class OrderInternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OrderInternalServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ru.dmitartur.common.grpc.OrdersProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OrderInternalService");
    }
  }

  private static final class OrderInternalServiceFileDescriptorSupplier
      extends OrderInternalServiceBaseDescriptorSupplier {
    OrderInternalServiceFileDescriptorSupplier() {}
  }

  private static final class OrderInternalServiceMethodDescriptorSupplier
      extends OrderInternalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    OrderInternalServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OrderInternalServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OrderInternalServiceFileDescriptorSupplier())
              .addMethod(getUpsertOrdersMethod())
              .build();
        }
      }
    }
    return result;
  }
}
