package com.graphaware.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * GraphAware Controller.
 */
@Controller
@RequestMapping(value = "/greeting")
public class HelloWorldController {

    @Autowired
    private GreetingService greetingService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String handleRequest() {
        return greetingService.greet();
    }
}
