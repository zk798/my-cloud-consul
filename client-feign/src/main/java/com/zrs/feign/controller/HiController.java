package com.zrs.feign.controller;

import com.zrs.feign.client.HiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HiController {

    @Autowired
    HiClient hiClient;

    @RequestMapping(value = "/hi",method = RequestMethod.GET)
    public String sayHi(@RequestParam String name){
        return hiClient.sayHiFromClientOne(name);
    }

    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    public String hello(){
        return hiClient.sayHello();
    }
}