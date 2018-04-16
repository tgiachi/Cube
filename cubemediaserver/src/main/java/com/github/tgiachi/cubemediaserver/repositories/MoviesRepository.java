package com.github.tgiachi.cubemediaserver.repositories;

import com.github.tgiachi.cubemediaserver.entities.MovieEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MoviesRepository extends MongoRepository<MovieEntity, String> {

    MovieEntity findByTitle(String title);
}
