package com.example.cassandrahomework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CassandraConfig {

    @Bean
    public CqlSession cqlSession(@Value("${cassandra.port}") int port){
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", port))
                .withLocalDatacenter("datacenter1")
                .build();
    }

}
