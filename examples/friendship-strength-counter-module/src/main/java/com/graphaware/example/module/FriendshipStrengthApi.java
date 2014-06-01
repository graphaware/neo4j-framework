package com.graphaware.example.module;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *  Simple REST API for finding the total friendship strength.
 */
@Controller
@RequestMapping("/friendship/strength")
public class FriendshipStrengthApi {

    private final FriendshipStrengthCounter counter;

    @Autowired
    public FriendshipStrengthApi(GraphDatabaseService database) {
        counter = new FriendshipStrengthCounter(database);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public long getTotalFriendshipStrength() {
        return counter.getTotalFriendshipStrength();
    }
}
