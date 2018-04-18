package com.github.tgiachi.cubemediaserver.data.events.ffmpeg;

import lombok.Data;

@Data
public class FFMpegBuildingEvent {

    double percentage;
    String filename;
    double fps;
    double speed;
}
