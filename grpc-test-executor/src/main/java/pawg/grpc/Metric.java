package pawg.grpc;

public record Metric(long restMillis, long restProtoMillis, long grpcMillis) {
    public static String csvHeaders() {
        return "REST;Protobuf;gRPC;X-Series";
    }
}
