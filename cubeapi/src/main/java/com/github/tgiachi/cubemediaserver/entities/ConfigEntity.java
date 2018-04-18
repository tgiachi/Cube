package com.github.tgiachi.cubemediaserver.entities;

import com.github.tgiachi.cubemediaserver.entities.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ConfigEntity extends BaseEntity {

    String language;

    List<DirectoryEntryEntity> directories;

    String ffmpegBinDirectory;

    List<String> subtitlesLanguages;

    /**
     * ctor
     */
    public ConfigEntity() {
        directories = new ArrayList<>();
        subtitlesLanguages = new ArrayList<>();
    }

    /**
     * add directory to list
     *
     * @param name
     * @param directory
     */
    public void addDirectory(String name, String directory, MediaFileTypeEnum type) {
        DirectoryEntryEntity entry = new DirectoryEntryEntity();

        entry.setDirectory(directory);
        entry.setName(name);
        entry.setMediaType(type);

        directories.add(entry);
    }
}
