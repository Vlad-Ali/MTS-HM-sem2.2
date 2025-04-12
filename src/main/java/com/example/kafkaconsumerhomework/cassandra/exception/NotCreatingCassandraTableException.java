package com.example.kafkaconsumerhomework.cassandra.exception;

public class NotCreatingCassandraTableException extends RuntimeException {
    public NotCreatingCassandraTableException(String message) {
        super(message);
    }
}
