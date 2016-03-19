package com.sambram.monitor.resources;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sambram.monitor.dao.service.JasDBService;
import com.sambram.monitor.dto.Greeting;

@Controller
public class TestController {
    
    @Autowired
    JasDBService jasDBService;
    
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public @ResponseBody Greeting greeting(@RequestParam(required=false, defaultValue="World") String name) {
        System.out.println("==== in greeting ====");
        Greeting greeting = new Greeting(counter.incrementAndGet(), String.format(template, name));
        jasDBService.addGreeting(greeting);
        return greeting;
    }
    
    @RequestMapping("/greeting/{id}")
    public @ResponseBody Greeting greeting(@PathVariable("id") Long id) {
        System.out.println("==== in greeting for Id ====");
        Greeting greeting = jasDBService.getGreeting(id);
        return greeting;
    }
    
}
