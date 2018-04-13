package com.github.tgiachi.cubemediaserver.interfaces.services;

import com.github.tgiachi.cubemediaserver.data.QueueTaskObject;

public interface IQueueExecutorService {

    void enqueueTask(QueueTaskObject runnable);
}
