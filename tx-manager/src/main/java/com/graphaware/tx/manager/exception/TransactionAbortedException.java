package com.graphaware.tx.manager.exception;

import org.neo4j.kernel.guard.GuardException;

public class TransactionAbortedException extends GuardException {

    public TransactionAbortedException(String message) {
        super(message);
    }

}
