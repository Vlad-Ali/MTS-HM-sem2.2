package com.example.kafkaconsumerhomework.cassandra.exception;

public class NotCreatingCassandraKeyspaceException extends RuntimeException {
    public NotCreatingCassandraKeyspaceException(String message) {
        super(message);
    }
}
