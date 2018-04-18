package com.github.tgiachi.cubemediaserver.mediaparsers;

import com.github.tgiachi.cubemediaserver.annotations.MediaParser;
import com.github.tgiachi.cubemediaserver.data.events.media.InputMediaFileEvent;
import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;
import com.github.tgiachi.cubemediaserver.interfaces.mediaparser.IMediaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.File;
import java.util.concurrent.Future;


//@MediaParser(extensions = {"xml"})
public class XmlMediaParser implements IMediaParser {

    private Logger mLogger = LoggerFactory.getLogger(XmlMediaParser.class);

    @Override
    public Future<ParsedMediaObject> Parse(InputMediaFileEvent inputMediaFileEvent) {
        ParsedMediaObject out = new ParsedMediaObject();

        long size = new File(inputMediaFileEvent.getFullPathFileName()).length();

        mLogger.info("File {} is {}", inputMediaFileEvent.getFilename(), size);

        return new AsyncResult<>(out);
    }
}
