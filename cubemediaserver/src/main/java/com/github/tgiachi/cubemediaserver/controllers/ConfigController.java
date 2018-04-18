package com.github.tgiachi.cubemediaserver.controllers;

import com.github.tgiachi.cubemediaserver.annotations.MediaParser;
import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import com.github.tgiachi.cubemediaserver.entities.DirectoryEntryEntity;
import com.github.tgiachi.cubemediaserver.entities.MediaFileTypeEnum;
import com.github.tgiachi.cubemediaserver.repositories.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/config")
public class ConfigController {

    @Autowired
    public ConfigRepository configRepository;


    @RequestMapping(value = "/watched/directories", method = RequestMethod.GET)
    public List<DirectoryEntryEntity> getWatchedDirectoies() {
        ConfigEntity config = configRepository.findAll().get(0);

        return config.getDirectories();
    }


    @RequestMapping(value = "/add/directory", method = RequestMethod.POST)
    public boolean addDirectoryToWatch(@RequestParam("name") String name, @RequestParam("directory") String directory, @RequestParam("mediaType") MediaFileTypeEnum mediaType) {
        ConfigEntity config = configRepository.findAll().get(0);

        long exists = config.getDirectories().stream().filter(s -> s.getDirectory().equals(directory)).count();

        if (exists == 0) {
            DirectoryEntryEntity entryEntity = new DirectoryEntryEntity();
            entryEntity.setDirectory(directory);
            entryEntity.setMediaType(mediaType);
            entryEntity.setName(name);

            config.getDirectories().add(entryEntity);

            configRepository.save(config);

            return true;
        } else {
            return false;
        }
    }
}
