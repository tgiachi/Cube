package com.github.tgiachi.cubemediaserver.repositories;

import com.github.tgiachi.cubemediaserver.entities.ConfigEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigRepository extends MongoRepository<ConfigEntity, String> {

}
