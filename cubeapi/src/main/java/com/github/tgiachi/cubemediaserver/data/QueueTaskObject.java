package com.github.tgiachi.cubemediaserver.data;


import lombok.Data;

@Data
public class QueueTaskObject {

    Class<?> sender;

    Runnable runnable;

    public QueueTaskObject Build(Class<?> sender, Runnable runnable)
    {
        this.sender = sender;
        this.runnable = runnable;

        return this;
    }
}
