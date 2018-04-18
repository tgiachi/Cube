package com.github.tgiachi.cubemediaserver.listeners;


import com.github.tgiachi.cubemediaserver.annotations.MediaEventListener;
import com.github.tgiachi.cubemediaserver.data.events.media.MediaAddedEvent;
import com.github.tgiachi.cubemediaserver.interfaces.eventlistener.IEventListener;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.List;

@MediaEventListener(eventClasses = {MediaAddedEvent.class})
public class SubtitleListener implements IEventListener<MediaAddedEvent> {


    private static URL serverUrl;
    private OpenSubtitlesImpl openSubtitlesClient;

    private Logger mLogger = LoggerFactory.getLogger(SubtitleListener.class);


    @PostConstruct
    public void init()
    {
        try
        {
            serverUrl = new URL("https", "api.opensubtitles.org", 443, "/xml-rpc");
            openSubtitlesClient = new OpenSubtitlesImpl(serverUrl);
            openSubtitlesClient.login("it", "AtomicStream");

        }
        catch (Exception ex)
        {

            mLogger.error("{}", ex);


        }

    }

    @Override
    public void onEvent(MediaAddedEvent object) {

        try
        {
            mLogger.info("Searching subtitle for file {}", object.getOutput().getFilename());

            List<SubtitleInfo> subs = openSubtitlesClient.searchSubtitles("ita", new File(object.getOutput().getFilename()));

            mLogger.info("Found {} subtitles", subs.size());
        }
        catch (Exception ex)
        {

        }



    }
}
