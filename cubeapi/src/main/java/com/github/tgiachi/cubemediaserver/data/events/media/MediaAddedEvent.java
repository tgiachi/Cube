package com.github.tgiachi.cubemediaserver.data.events.media;

import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;
import lombok.Data;

@Data
public class MediaAddedEvent {

    String mediaId;
    ParsedMediaObject output;
}
