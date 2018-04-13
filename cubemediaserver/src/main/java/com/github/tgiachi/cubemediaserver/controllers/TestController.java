package com.github.tgiachi.cubemediaserver.controllers;

import com.github.tgiachi.cubemediaserver.services.MediaParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @Autowired
    public MediaParserService mediaParserService;

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String testGet()
    {
        return "ok";

    }
}
