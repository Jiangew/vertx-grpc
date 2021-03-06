package examples;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;
import io.vertx.docgen.Source;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Source
public class Examples {

  public void simpleServer(Vertx vertx) throws Exception {
    // The rcp service
    GreeterGrpc.GreeterVertxImplBase service = new GreeterGrpc.GreeterVertxImplBase() {
      @Override
      public void sayHello(HelloRequest request, Future<HelloReply> future) {
        future.complete(HelloReply.newBuilder().setMessage(request.getName()).build());
      }
    };
  }

  public void startServer(BindableService service, Vertx vertx) throws Exception {
    // Create the server
    VertxServer rpcServer = VertxServerBuilder
      .forAddress(vertx, "my.host", 8080)
      .addService(service)
      .build();

    // Start is asynchronous
    rpcServer.start();
  }


  public void connectClient(Vertx vertx) {
    // Create the channel
    ManagedChannel channel = VertxChannelBuilder
      .forAddress(vertx, "localhost", 8080)
      .usePlaintext(true)
      .build();

    // Get a stub to use for interacting with the remote service
    GreeterGrpc.GreeterVertxStub stub = GreeterGrpc.newVertxStub(channel);
  }

  public void simpleClient(GreeterGrpc.GreeterVertxStub stub) {
    // Make a request
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();

    // Call the remote service
    stub.sayHello(request, ar -> {
      if (ar.succeeded()) {
        System.out.println("Got the server response: " + ar.result().getMessage());
      } else {
        System.out.println("Coult not reach server " + ar.cause().getMessage());
      }
    });
  }

  public void sslServer(Vertx vertx) {
    VertxServerBuilder builder = VertxServerBuilder.forPort(vertx, 8080)
        .useSsl(options -> options
            .setSsl(true)
            .setUseAlpn(true)
            .setKeyStoreOptions(new JksOptions()
                .setPath("server-keystore.jks")
                .setPassword("secret")));
  }

  public void sslClient(Vertx vertx) {
    ManagedChannel channel = VertxChannelBuilder.
        forAddress(vertx, "localhost", 8080)
        .useSsl(options -> options
            .setSsl(true)
            .setUseAlpn(true)
            .setTrustStoreOptions(new JksOptions()
                .setPath("client-truststore.jks")
                .setPassword("secret")))
        .build();
  }
}
