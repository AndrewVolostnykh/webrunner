package andrew_volostnykh.webrunner.service.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcReflectionService {

	public CompletableFuture<Map<String, List<String>>> getAllServicesAndMethodsAsync(
		String host
	) {
		CompletableFuture<Map<String, List<String>>> future = new CompletableFuture<>();

		listServices(host)
			.thenAccept(serviceNames -> {
				if (serviceNames.isEmpty()) {
					future.completeExceptionally(new RuntimeException("No gRPC services found"));
					return;
				}

				Map<String, List<String>> result = new LinkedHashMap<>();
				CompletableFuture<?>[] methodFutures = new CompletableFuture<?>[serviceNames.size()];

				AtomicInteger index = new AtomicInteger(0);

				for (String serviceName : serviceNames) {
					int pos = index.getAndIncrement();
					methodFutures[pos] = listMethods(host, serviceName)
						.thenAccept(methods -> result.put(serviceName, methods))
						.exceptionally(ex -> {
							result.put(serviceName, List.of("⚠ ERROR: " + ex.getMessage()));
							return null;
						});
				}

				CompletableFuture
					.allOf(methodFutures)
					.thenRun(() -> future.complete(result));
			})
			.exceptionally(ex -> {
				future.completeExceptionally(ex);
				return null;
			});

		return future;
	}

	private CompletableFuture<List<String>> listServices(
		String host
	) {
		CompletableFuture<List<String>> future = new CompletableFuture<>();

		ManagedChannel channel = ManagedChannelBuilder
			.forTarget(host)
			.usePlaintext()
			.build();

		ServerReflectionGrpc.ServerReflectionStub asyncStub =
			ServerReflectionGrpc.newStub(channel);

		List<String> services = new ArrayList<>();

		StreamObserver<ServerReflectionResponse> responseObserver =
			new StreamObserver<>() {
				@Override
				public void onNext(ServerReflectionResponse response) {
					if (response.hasListServicesResponse()) {
						response.getListServicesResponse().getServiceList()
							.forEach(s -> services.add(s.getName()));
					}
				}

				@Override
				public void onError(Throwable t) {
					future.completeExceptionally(t);
					channel.shutdownNow();
				}

				@Override
				public void onCompleted() {
					future.complete(services);
					channel.shutdownNow();
				}
			};

		StreamObserver<ServerReflectionRequest> requestObserver =
			asyncStub.serverReflectionInfo(responseObserver);

		requestObserver.onNext(
			ServerReflectionRequest.newBuilder()
				.setListServices("")
				.build()
		);
		requestObserver.onCompleted();

		return future;
	}

	private CompletableFuture<List<String>> listMethods(
		String host,
		String serviceName
	) {
		CompletableFuture<List<String>> future = new CompletableFuture<>();

		ManagedChannel channel = ManagedChannelBuilder
			.forTarget(host)
			.usePlaintext()
			.build();

		ServerReflectionGrpc.ServerReflectionStub asyncStub =
			ServerReflectionGrpc.newStub(channel);

		List<String> methods = new ArrayList<>();

		StreamObserver<ServerReflectionResponse> responseObserver =
			new StreamObserver<>() {
				@Override
				public void onNext(ServerReflectionResponse resp) {
					if (!resp.hasFileDescriptorResponse()) {
						return;
					}

					try {
						Map<String, DescriptorProtos.FileDescriptorProto> protoMap = new HashMap<>();
						for (ByteString bytes : resp.getFileDescriptorResponse().getFileDescriptorProtoList()) {
							DescriptorProtos.FileDescriptorProto fdProto =
								DescriptorProtos.FileDescriptorProto.parseFrom(bytes);
							protoMap.put(fdProto.getName(), fdProto);
						}

						Map<String, Descriptors.FileDescriptor> built = new HashMap<>();

						class RecursiveBuilder {

							Descriptors.FileDescriptor build(String name)
								throws Descriptors.DescriptorValidationException {
								if (built.containsKey(name)) {
									return built.get(name);
								}

								DescriptorProtos.FileDescriptorProto proto = protoMap.get(name);
								if (proto == null) {
									return null;
								}

								List<Descriptors.FileDescriptor> deps = new ArrayList<>();
								for (String depName : proto.getDependencyList()) {
									Descriptors.FileDescriptor dep = build(depName);
									if (dep != null) {
										deps.add(dep);
									}
								}

								Descriptors.FileDescriptor fd =
									Descriptors.FileDescriptor.buildFrom(
										proto,
										deps.toArray(new Descriptors.FileDescriptor[0])
									);

								built.put(name, fd);
								return fd;
							}
						}

						RecursiveBuilder builder = new RecursiveBuilder();

						for (String fileName : protoMap.keySet()) {
							builder.build(fileName);
						}

						for (Descriptors.FileDescriptor fd : built.values()) {
							for (Descriptors.ServiceDescriptor service : fd.getServices()) {
								if (service.getFullName().equals(serviceName)) {
									service.getMethods().forEach(m -> methods.add(m.getName()));
								}
							}
						}

					} catch (Exception e) {
						future.completeExceptionally(e);
					}
				}

				@Override
				public void onError(Throwable t) {
					future.completeExceptionally(t);
					channel.shutdownNow();
				}

				@Override
				public void onCompleted() {
					future.complete(methods);
					channel.shutdownNow();
				}
			};

		StreamObserver<ServerReflectionRequest> requestObserver =
			asyncStub.serverReflectionInfo(responseObserver);

		requestObserver.onNext(
			ServerReflectionRequest.newBuilder()
				.setFileContainingSymbol(serviceName)
				.build()
		);
		requestObserver.onCompleted();

		return future;
	}
}
