package com.graphaware.tx.manager.api;

import com.graphaware.tx.manager.Transaction;
import com.graphaware.tx.manager.TransactionManager;
import com.graphaware.tx.manager.TransactionManagerImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
@Transactional
public class TransactionManagerController {

    private final TransactionManager transactionMonitor;

    @Autowired
    public TransactionManagerController(GraphDatabaseService database) {
        transactionMonitor = new TransactionManagerImpl(database);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Transaction> list() {
        return transactionMonitor.list();
    }

    @RequestMapping(value="/{uuid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Transaction get(@PathVariable UUID uuid) {
        return transactionMonitor.get(uuid);
    }

    @RequestMapping(value="/{uuid}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void abort(@PathVariable UUID uuid) {
        transactionMonitor.abort(uuid);
    }

}
