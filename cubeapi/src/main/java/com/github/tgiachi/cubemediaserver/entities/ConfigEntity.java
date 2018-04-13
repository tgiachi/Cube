package com.github.tgiachi.cubemediaserver.entities;

import com.github.tgiachi.cubemediaserver.entities.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;


@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ConfigEntity extends BaseEntity {

    String language;

    Map<String, String> directories;

    /**
     * ctor
     */
    public ConfigEntity() {
        directories = new HashMap<>();
    }

    /**
     * add directory to list
     *
     * @param name
     * @param directory
     */
    public void addDirectory(String name, String directory) {
        directories.put(name, directory);
    }
}
