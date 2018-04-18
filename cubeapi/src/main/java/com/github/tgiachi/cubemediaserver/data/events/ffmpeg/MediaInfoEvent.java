package com.github.tgiachi.cubemediaserver.data.events.ffmpeg;


import lombok.Data;

@Data
public class MediaInfoEvent {
    String filename;
    double duration;
    String codec;

    int width;
    int height;
}
