package com.github.tgiachi.cubemediaserver.data.media;

import com.github.tgiachi.cubemediaserver.entities.MediaFileTypeEnum;
import lombok.Data;

@Data
public class ParsedMediaObject {


    boolean isParsed;

    boolean isSavedOnDb;

    String mediaId;

    String filename;

    MediaFileTypeEnum mediaType;
}
