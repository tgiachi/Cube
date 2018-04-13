package com.github.tgiachi.cubemediaserver.services;

import com.github.tgiachi.cubemediaserver.annotations.MediaParser;
import com.github.tgiachi.cubemediaserver.interfaces.mediaparser.IMediaParser;
import com.github.tgiachi.cubemediaserver.interfaces.services.IMediaParserService;
import com.github.tgiachi.cubemediaserver.interfaces.services.IQueueExecutorService;
import com.github.tgiachi.cubemediaserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.util.Set;


@Component
public class MediaParserService implements IMediaParserService {

    private final Logger mLogger = LoggerFactory.getLogger(MediaParserService.class);

    @Autowired
    public IQueueExecutorService mQueueExecutorService;

    @Autowired
    public ApplicationContext mApplicationContext;


    @PostConstruct
    public void init()
    {
        mLogger.info("Scanning for MediaParsers");

        Set<Class<?>> types = ReflectionUtils.getAnnotation(MediaParser.class);

        mLogger.info("Found {} media parsers", types.size());

        types.stream().forEach(this::initParser);
    }

    private void initParser(Class<?> classz)
    {
        try
        {

            if (classz.isAssignableFrom(IMediaParser.class))
            {

            }
            else
            {
                mLogger.warn("Class {} don't implements interface IMediaParser", classz.getName());
            }

        }
        catch (Exception ex)
        {
            mLogger.error("Error during init parser: {} => {}", classz.getName(), ex.getMessage());
        }

    }




}
