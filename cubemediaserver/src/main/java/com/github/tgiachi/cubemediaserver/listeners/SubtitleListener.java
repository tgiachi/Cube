package com.github.tgiachi.cubemediaserver.listeners;


import com.github.tgiachi.cubemediaserver.annotations.MediaEventListener;
import com.github.tgiachi.cubemediaserver.data.events.media.MediaAddedEvent;
import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import com.github.tgiachi.cubemediaserver.interfaces.eventlistener.IEventListener;
import com.github.tgiachi.cubemediaserver.repositories.ConfigRepository;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@MediaEventListener(eventClasses = {MediaAddedEvent.class})
public class SubtitleListener implements IEventListener<MediaAddedEvent> {


    private static URL serverUrl;
    private OpenSubtitlesImpl openSubtitlesClient;

    private Logger mLogger = LoggerFactory.getLogger(SubtitleListener.class);

    private boolean isLogged = false;
    @Autowired
    public ConfigRepository configRepository;

    @PostConstruct
    public void init() {
        try {
            serverUrl = new URL("https", "api.opensubtitles.org", 443, "/xml-rpc");
            openSubtitlesClient = new OpenSubtitlesImpl(serverUrl);
            openSubtitlesClient.login("ita", "TemporaryUserAgent");
            isLogged = true;

        } catch (Exception ex) {
            mLogger.error("Error during login in OpenSubTitles -> {}", ex.getMessage());
        }
    }

    @Override
    public void onEvent(MediaAddedEvent object) {

        try {
            if (isLogged) {
                mLogger.info("Searching subtitle for file {}", object.getOutput().getFilename());

                for (String lang : getSubtitlesLanguages()) {

                    List<SubtitleInfo> subs = openSubtitlesClient.searchSubtitles(lang, new File(object.getOutput().getFilename()));

                    mLogger.info("Found {} subtitles languange {}", subs.size(), lang);
                }

                openSubtitlesClient.logout();
            } else {
                mLogger.warn("Can't find sub titles, client not logged");
            }

        } catch (Exception ex) {

        }

    }

    private List<String> getSubtitlesLanguages() {

        List<String> langs = new ArrayList<>();
        ConfigEntity configEntity = configRepository.findAll().get(0);

        try {
            Locale locale = new Locale(configEntity.getLanguage());
            langs.add(locale.getISO3Language());
        } catch (Exception ex) {
            return null;
        }

        return langs;

    }
}
