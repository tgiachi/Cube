package com.github.tgiachi.cubemediaserver.entities;

import com.github.tgiachi.cubemediaserver.entities.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.UUID;

@EqualsAndHashCode(callSuper=false)
@ToString
@Data
public class MovieEntity extends BaseEntity {

    String uid;

    @Indexed(name = "title_idx")
    String title;

    Integer year;

    String directoryEntry;

    String filename;

    int movieDbId;

    public MovieEntity()
    {
        uid = UUID.randomUUID().toString().replace("-", "");
    }
}
