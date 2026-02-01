package org.example.orderservice.config;

import com.booking.inventory.grpc.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Value("${inventory.grpc.host:localhost}")
    private String inventoryHost;

    @Value("${inventory.grpc.port:9090}")
    private int inventoryPort;

    @Bean
    public ManagedChannel inventoryChannel() {
        return ManagedChannelBuilder.forAddress(inventoryHost, inventoryPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceBlockingStub(ManagedChannel inventoryChannel) {
        return InventoryServiceGrpc.newBlockingStub(inventoryChannel);
    }
}
