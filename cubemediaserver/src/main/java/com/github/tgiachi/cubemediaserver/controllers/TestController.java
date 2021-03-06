package com.github.tgiachi.cubemediaserver.controllers;

import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import com.github.tgiachi.cubemediaserver.repositories.ConfigRepository;
import com.github.tgiachi.cubemediaserver.services.MediaParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @Autowired
    public MediaParserService mediaParserService;

    @Autowired
    public ConfigRepository configRepository;

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String testGet()
    {
        return "ok";

    }


    @RequestMapping(value = "/scan", method = RequestMethod.GET)
    public String testDirectories()
    {
        ConfigEntity cfg = configRepository.findAll().get(0);

        mediaParserService.fullScanDirectory(cfg.getDirectories().get(0));

        return "ok";
    }
}
