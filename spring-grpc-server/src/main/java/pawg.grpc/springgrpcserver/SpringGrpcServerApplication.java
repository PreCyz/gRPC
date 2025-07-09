package pawg.grpc.springgrpcserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.codec.protobuf.ProtobufEncoder;

@SpringBootApplication
public class SpringGrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGrpcServerApplication.class, args);
    }

    @Bean
    public ProtobufDecoder protobufDecoder() {
        return new ProtobufDecoder();
    }

    @Bean
    public ProtobufEncoder protobufEncoder() {
        return new ProtobufEncoder();
    }

}
