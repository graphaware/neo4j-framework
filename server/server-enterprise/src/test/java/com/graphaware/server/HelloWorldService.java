package com.graphaware.server;

import org.springframework.stereotype.Service;

@Service
public class HelloWorldService implements GreetingService {

    @Override
    public String greet() {
        return "Hello World";
    }
}
