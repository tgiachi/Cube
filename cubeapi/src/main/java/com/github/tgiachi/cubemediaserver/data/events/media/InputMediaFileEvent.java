package com.github.tgiachi.cubemediaserver.data.events.media;

import com.github.tgiachi.cubemediaserver.entities.DirectoryEntryEntity;
import com.github.tgiachi.cubemediaserver.entities.MediaFileTypeEnum;
import lombok.Data;

@Data
public class InputMediaFileEvent {

    DirectoryEntryEntity directoryEntry;

    String filename;

    String fullPathFileName;

    MediaFileTypeEnum mediaType;


}
