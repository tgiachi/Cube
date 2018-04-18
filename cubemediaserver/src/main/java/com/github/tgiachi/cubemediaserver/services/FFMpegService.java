package com.github.tgiachi.cubemediaserver.services;

import com.github.tgiachi.cubemediaserver.data.events.ffmpeg.FFMpegBuildingEvent;
import com.github.tgiachi.cubemediaserver.data.events.ffmpeg.MediaInfoEvent;
import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import com.github.tgiachi.cubemediaserver.interfaces.services.IFFMpegService;
import com.github.tgiachi.cubemediaserver.repositories.ConfigRepository;
import com.github.tgiachi.cubemediaserver.utils.CompressionUtils;
import com.github.tgiachi.cubemediaserver.utils.HttpUtils;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Component
public class FFMpegService implements IFFMpegService {

    private static Logger mLogger = LoggerFactory.getLogger(FFMpegService.class);

    private String mFfmpegDirectory;

    private String mRootDirectory;

    @Autowired
    public ConfigRepository configRepository;


    @PostConstruct
    public void init() {

        boolean isFfmpegFound = false;

        mLogger.info("Check if FFMpeg is installed");

        if (!isFFMpegInstalled()) {
            if (System.getenv("ffmpeg") != null) {
                mFfmpegDirectory = System.getenv("ffmpeg");
                mLogger.info("Found FFMpeg in env variabile = {}", mFfmpegDirectory);
                isFfmpegFound = true;
            } else {
                checkDirectoryRoot();
            }

            if (!isFfmpegFound) {
                downloadFFMpeg();
            }
        } else {
            mLogger.info("FFMPEG Installed");
        }

        //SystemUtils.IS_OS_WINDOWS
    }

    private void checkDirectoryRoot() {
        String userDirectory = System.getProperty("user.dir");

        mRootDirectory = userDirectory + File.separator + "cube" + File.separator;

        mFfmpegDirectory = mRootDirectory + "ffmpeg" + File.separator;

        if (!new File(mRootDirectory).exists()) {
            mLogger.info("Creating directory {}", mRootDirectory);

            new File(mRootDirectory).mkdirs();
        }

        if (!new File(mFfmpegDirectory).exists()) {
            mLogger.info("Creating directory {}", mFfmpegDirectory);
            new File(mFfmpegDirectory).mkdirs();
        }
    }

    private void downloadFFMpeg() {
        String downloadUrl = "https://ffmpeg.zeranoe.com/builds/%s/static/ffmpeg-3.4.2-%s-static.zip";
        String outFilename = System.getProperty("java.io.tmpdir") + "ffmpeg-3.4.2-%s-static.zip";

        if (SystemUtils.IS_OS_WINDOWS) {
            downloadUrl = String.format(downloadUrl, "win64", "win64");
            outFilename = String.format(outFilename, "win64");
        }

        if (SystemUtils.IS_OS_MAC_OSX) {
            downloadUrl = String.format(downloadUrl, "macos64", "macos64");
            outFilename = String.format(outFilename, "macos64");
        }

        mLogger.info("Downloading {}...", outFilename);

        try {
            HttpUtils.downloadFile(downloadUrl, outFilename);
            mLogger.info("Download completed!");

            boolean result = CompressionUtils.unzipFile(outFilename, mFfmpegDirectory);

            Collection<File> files = FileUtils.listFiles(new File(mFfmpegDirectory), null, true);


            files.forEach(file -> {
                if (file.toString().contains("bin")) {
                    String ffMpeg = Paths.get(file.toString()).getParent().toString();

                    mLogger.info("FFMpeg root directory -> {}", ffMpeg);

                    saveFFMpegDirectory(ffMpeg);
                    return;
                }
            });


        } catch (Exception ex) {
            mLogger.error("Error during download file {} => {}", outFilename, ex);
        }
    }

    private void saveFFMpegDirectory(String directory) {
        ConfigEntity entity = configRepository.findAll().get(0);

        entity.setFfmpegBinDirectory(directory);

        configRepository.save(entity);
    }

    private String getFFMpegPath() {
        ConfigEntity entity = configRepository.findAll().get(0);

        return entity.getFfmpegBinDirectory() + File.separator;

    }

    private boolean isFFMpegInstalled() {
        ConfigEntity entity = configRepository.findAll().get(0);

        return entity.getFfmpegBinDirectory() != null;
    }

    private FFmpeg getFFMpegInstance() {
        String ext = "";

        if (SystemUtils.IS_OS_WINDOWS)
            ext = ".exe";

        try {
            return new FFmpeg(getFFMpegPath() + "ffmpeg" + ext);
        } catch (Exception ex) {

            return null;
        }

    }

    private FFprobe getFFprobeInstance() {
        String ext = "";

        if (SystemUtils.IS_OS_WINDOWS)
            ext = ".exe";

        try {
            return new FFprobe(getFFMpegPath() + "ffprobe" + ext);
        } catch (Exception ex) {
            return null;
        }

    }

    @Override
    public MediaInfoEvent getMediaInformation(String filename) {
        try {
            MediaInfoEvent mediaInfoEvent = new MediaInfoEvent();
            FFmpegProbeResult probeResult = getFFprobeInstance().probe(filename);

            FFmpegStream stream = probeResult.getStreams().get(0);
            mediaInfoEvent.setCodec(stream.codec_name);
            mediaInfoEvent.setFilename(filename);
            mediaInfoEvent.setDuration(probeResult.getFormat().duration);
            mediaInfoEvent.setWidth(stream.width);
            mediaInfoEvent.setHeight(stream.height);



            return mediaInfoEvent;

        } catch (Exception ex) {

        }

        return null;

    }

    @Override
    public void getMediaInformationAsync(String filename, Function<MediaInfoEvent, Void> callback) {
        Executors.newCachedThreadPool().submit(() -> {

            MediaInfoEvent event = getMediaInformation(filename);
            callback.apply(event);
        });
    }

    private FFmpegExecutor getFFMpegExecutor()
    {
        FFmpegExecutor executor = new FFmpegExecutor(getFFMpegInstance(), getFFprobeInstance());

        return executor;
    }

    private void executeFFMpegBuilder(FFmpegBuilder builder, String filename)
    {
        try
        {
            FFmpegProbeResult in = getFFprobeInstance().probe(filename);

            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            getFFMpegExecutor().createJob(builder, progress -> {

                double percentage = progress.out_time_ns / duration_ns;

                FFMpegBuildingEvent ffMpegBuildingEvent = new FFMpegBuildingEvent();

                ffMpegBuildingEvent.setFilename(filename);
                ffMpegBuildingEvent.setFps(progress.fps.doubleValue());
                ffMpegBuildingEvent.setSpeed(progress.speed);
                ffMpegBuildingEvent.setPercentage(percentage);
            });
        }
        catch (Exception ex)
        {

        }

    }
}
