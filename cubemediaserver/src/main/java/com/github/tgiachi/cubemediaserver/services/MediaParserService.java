package com.github.tgiachi.cubemediaserver.services;

import com.github.tgiachi.cubemediaserver.annotations.MediaParser;
import com.github.tgiachi.cubemediaserver.data.QueueTaskObject;
import com.github.tgiachi.cubemediaserver.data.events.media.InputMediaFileEvent;
import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;
import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import com.github.tgiachi.cubemediaserver.entities.DirectoryEntryEntity;
import com.github.tgiachi.cubemediaserver.interfaces.mediaparser.IMediaParser;
import com.github.tgiachi.cubemediaserver.interfaces.services.IMediaParserService;
import com.github.tgiachi.cubemediaserver.interfaces.services.IQueueExecutorService;
import com.github.tgiachi.cubemediaserver.repositories.ConfigRepository;
import com.github.tgiachi.cubemediaserver.utils.EventBusUtils;
import com.github.tgiachi.cubemediaserver.utils.ReflectionUtils;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;


import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Future;


@Component
public class MediaParserService implements IMediaParserService {

    private final Logger mLogger = LoggerFactory.getLogger(MediaParserService.class);

    @Autowired
    public IQueueExecutorService mQueueExecutorService;

    @Autowired
    public ApplicationContext mApplicationContext;


    @Autowired
    public ConfigRepository mConfigRepository;

    private Thread mWatchThread;

    private WatchService mWatchService;

    private ConfigEntity configEntity;

    private boolean isWatchServiceEnabled = true;

    private Map<WatchKey, DirectoryEntryEntity> mWatchMap = new HashMap<>();


    private HashMap<String, List<Class<?>>> mMediaParsers;


    public MediaParserService() {
        mMediaParsers = new HashMap<>();
    }

    @PostConstruct
    public void init() {

        subscriveEvents();

        scanMediaParsers();
        loadConfig();
        initFileWatchers();


    }

    private void subscriveEvents() {
        EventBusUtils.getSingleton().register(this);
    }

    private void loadConfig() {
        if (mConfigRepository.findAll().size() == 0) {
            mLogger.warn("Config is empty, creating default");

            Locale locale = Locale.getDefault();
            configEntity = new ConfigEntity();

            configEntity.setLanguage(locale.getLanguage());

            mConfigRepository.save(configEntity);
        } else {
            configEntity = mConfigRepository.findAll().get(0);
        }
    }

    private Runnable getWatchRunnable() {
        return () -> {
            WatchKey key;
            try {
                while ((key = mWatchService.take()) != null || isWatchServiceEnabled) {
                    for (WatchEvent<?> event : key.pollEvents()) {

                        DirectoryEntryEntity directoryEntryEntity = mWatchMap.get(key);

                        InputMediaFileEvent inputEvent = new InputMediaFileEvent();

                        inputEvent.setDirectoryEntry(directoryEntryEntity);
                        inputEvent.setMediaType(directoryEntryEntity.getMediaType());
                        inputEvent.setFilename(event.context().toString());
                        inputEvent.setFullPathFileName(String.format("%s%s%s", directoryEntryEntity.getDirectory(), File.separator, inputEvent.getFilename()));

                        broadcastInputEvent(inputEvent);
                    }
                    key.reset();
                }
            } catch (Exception ex) {

            }
        };
    }


    private void broadcastInputEvent(InputMediaFileEvent inputMediaFileEvent) {
        EventBusUtils.getSingleton().broadcast(inputMediaFileEvent);
    }

    private void initFileWatchers() {


        try {
            mWatchService = FileSystems.getDefault().newWatchService();

        } catch (Exception ex) {
            mLogger.error("Error during getting watch service: {}", ex.getMessage());
        }

        for (DirectoryEntryEntity directoryEntryEntity : configEntity.getDirectories()) {
            mLogger.info("Registering hook for directory: {} ({})", directoryEntryEntity.getDirectory(), directoryEntryEntity.getMediaType());

            Path path = Paths.get(directoryEntryEntity.getDirectory());


            try {
                mWatchMap.put(path.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE), directoryEntryEntity);

            } catch (Exception ex) {
                mLogger.error("Error during watch directory {} => {}", directoryEntryEntity.getDirectory(), ex.getMessage());
            }
        }

        mWatchThread = new Thread(getWatchRunnable());
        mWatchThread.start();

    }

    private void scanMediaParsers() {
        mLogger.info("Scanning for MediaParsers");

        Set<Class<?>> types = ReflectionUtils.getAnnotation(MediaParser.class);

        mLogger.info("Found {} media parsers", types.size());

        types.stream().forEach(this::initParser);
    }

    private void initParser(Class<?> classz) {
        try {
            if (IMediaParser.class.isAssignableFrom(classz)) {
                MediaParser ann = classz.getAnnotation(MediaParser.class);


                for (String ext : ann.extensions()) {

                    if (!mMediaParsers.containsKey(ext))
                        mMediaParsers.put(ext, new ArrayList<>());


                    mMediaParsers.get(ext).add(classz);
                }
                mLogger.info("Registering class {} for extensions {}", classz.getSimpleName(), Arrays.toString(ann.extensions()));
                registerBean(classz);
            } else {
                mLogger.warn("Class {} don't implements interface IMediaParser", classz.getName());
            }

        } catch (Exception ex) {
            mLogger.error("Error during init parser: {} => {}", classz.getName(), ex.getMessage());
        }

    }


    private void parseFile(InputMediaFileEvent inputMediaFileEvent) {
        try {

            mLogger.info("New file input: {} ({})", inputMediaFileEvent.getFullPathFileName(), inputMediaFileEvent.getMediaType());

            String ext = FilenameUtils.getExtension(inputMediaFileEvent.getFilename());

            if (mMediaParsers.containsKey(ext)) {
                List<Class<?>> parsers = mMediaParsers.get(ext);

                mLogger.info("Found {} media parsers for extension {}", parsers.size(), ext);

                for (Class<?> parser : parsers) {
                    try {
                        IMediaParser mediaParser = (IMediaParser) mApplicationContext.getBean(parser);

                        Future<ParsedMediaObject> fOut = mediaParser.Parse(inputMediaFileEvent);

                        mQueueExecutorService.enqueueTask(new QueueTaskObject().Build(this.getClass(), () -> {

                            try {
                                ParsedMediaObject parsedMediaObject = fOut.get();
                            } catch (Exception ex) {

                            }


                        }));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                }

            }

        } catch (Exception ex) {

        }
    }


    public void fullScanDirectory(DirectoryEntryEntity directoryEntryEntity) {

        Collection<File> files = FileUtils.listFiles(new File(directoryEntryEntity.getDirectory()), null, true);

        files.forEach(file -> {
            InputMediaFileEvent event = new InputMediaFileEvent();
            event.setDirectoryEntry(directoryEntryEntity);
            event.setFilename(FilenameUtils.getName(file.getName()));
            event.setMediaType(directoryEntryEntity.getMediaType());
            event.setFullPathFileName(file.getName());
            broadcastInputEvent(event);
        });

    }


    private void registerBean(Class<?> classz) {
        ((GenericWebApplicationContext) mApplicationContext).registerBean(classz);
    }


    @Subscribe
    public void onInputMediaFile(InputMediaFileEvent event) {
        mQueueExecutorService.enqueueTask(new QueueTaskObject().Build(this.getClass(), () -> {
            parseFile(event);
        }));

    }


}
