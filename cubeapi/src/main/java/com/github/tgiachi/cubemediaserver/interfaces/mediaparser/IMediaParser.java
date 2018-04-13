package com.github.tgiachi.cubemediaserver.interfaces.mediaparser;


import com.github.tgiachi.cubemediaserver.data.media.ParsedMediaObject;

import java.util.concurrent.Future;

/**
 * Interface for create media parsers
 */
public interface IMediaParser {

    /**
     * Parse media
     * @param filename
     * @return
     */
    Future<ParsedMediaObject> Parse(String filename);
}
