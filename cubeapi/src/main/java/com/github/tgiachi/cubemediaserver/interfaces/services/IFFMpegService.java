package com.github.tgiachi.cubemediaserver.interfaces.services;

import com.github.tgiachi.cubemediaserver.data.events.ffmpeg.MediaInfoEvent;

import java.util.concurrent.Callable;
import java.util.function.Function;

public interface IFFMpegService {

    MediaInfoEvent getMediaInformation(String filename);

    void getMediaInformationAsync(String filename, Function<MediaInfoEvent, Void> callback);
}
