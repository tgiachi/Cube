package com.github.tgiachi.cubemediaserver.interfaces.mediaparser;


import com.github.tgiachi.cubemediaserver.data.events.media.InputMediaFileEvent;
import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;

import java.util.concurrent.Future;

/**
 * Interface for create media parsers
 */
public interface IMediaParser {

    /**
     * Scan file
     * @param inputMediaFileEvent
     * @return
     */
    Future<ParsedMediaObject> Parse(InputMediaFileEvent inputMediaFileEvent);
}
