package com.github.tgiachi.cubemediaserver.entities.base;


import lombok.Data;

import java.util.Date;

/**
 * Base NoSQL entity
 */
@Data
public class BaseEntity {

    String id;

    Date createDateTime;

    Date updatedDatetime;

    /**
     * ctor
     */
    public BaseEntity()
    {
        createDateTime = new Date();
    }
}
