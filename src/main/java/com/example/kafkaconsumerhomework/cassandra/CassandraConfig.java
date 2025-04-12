package com.example.kafkaconsumerhomework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.kafkaconsumerhomework.cassandra.exception.NotCreatingCassandraKeyspaceException;
import com.example.kafkaconsumerhomework.cassandra.exception.NotCreatingCassandraTableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CassandraConfig {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraConfig.class);

    @Bean
    public CqlSession cqlSession(@Value("${cassandra.port}") int port){
        CqlSession cqlSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", port))
                .withLocalDatacenter("datacenter1")
                .build();
        try {
            String query = "CREATE KEYSPACE IF NOT EXISTS my_keyspace WITH replication = "
                    + "{'class':'SimpleStrategy', 'replication_factor':1};";
            cqlSession.execute(query);
            LOG.debug("Keyspace 'my_keyspace' created successfully.");
        } catch (Exception e) {
            LOG.error("Failed to create keyspace: " + e.getMessage());
            throw new NotCreatingCassandraKeyspaceException(e.getMessage());
        }

        try {
            String query = "CREATE TABLE IF NOT EXISTS my_keyspace.user_audit (" +
                    "user_id UUID," +
                    "event_time TIMESTAMP," +
                    "event_type TEXT," +
                    "event_details TEXT," +
                    "PRIMARY KEY ((user_id), event_time)" +
                    ") WITH CLUSTERING ORDER BY (event_time DESC);";
            cqlSession.execute(query);
            LOG.debug("Table 'user_audit' created successfully.");
        } catch (Exception e) {
            LOG.error("Failed to create table: " + e.getMessage());
            throw new NotCreatingCassandraTableException(e.getMessage());
        }
        return cqlSession;
    }

}
