package com.github.tgiachi.cubemediaserver.entities.base;

import com.github.tgiachi.cubemediaserver.entities.MediaFileTypeEnum;
import lombok.Data;

@Data
public class DirectoryEntryEntity {

    String directory;

    String name;

    MediaFileTypeEnum mediaType;

}
