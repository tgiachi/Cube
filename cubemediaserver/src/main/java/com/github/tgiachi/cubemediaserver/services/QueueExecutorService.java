package com.github.tgiachi.cubemediaserver.services;

import com.github.tgiachi.cubemediaserver.data.QueueTaskObject;
import com.github.tgiachi.cubemediaserver.data.events.QueueTaskEvent;
import com.github.tgiachi.cubemediaserver.interfaces.services.IQueueExecutorService;
import com.github.tgiachi.cubemediaserver.utils.EventBusUtils;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * Implementation of Queue Executor Service
 */
@Component
public class QueueExecutorService implements IQueueExecutorService {

    private final Logger mLogger = LoggerFactory.getLogger(QueueExecutorService.class);

    private final int MaxQueueProcessors = 5;

    private final ExecutorService mExecutorService;

    private final LinkedBlockingQueue<QueueTaskObject> mQueue = new LinkedBlockingQueue<>();

    private boolean isQueueEnabled = true;


    public QueueExecutorService() {

        mExecutorService = Executors.newFixedThreadPool(MaxQueueProcessors);

        mLogger.info("Starting executor queue with {} processors", MaxQueueProcessors);

        for (int i = 0; i < MaxQueueProcessors; i++) {
            mExecutorService.execute(buildQueueProcessor());
        }

        EventBusUtils.getSingleton().register(this);
    }

    @Subscribe
    public void onQueueTaskEvent(QueueTaskEvent event) {

        if (event.getQueueTaskObject() != null)
            enqueueTask(event.getQueueTaskObject());
    }

    public void enqueueTask(QueueTaskObject runnable) {
        synchronized (mQueue) {
            mLogger.trace("Adding runnable in queue from class {}", runnable.getSender().getSimpleName());
            mQueue.add(runnable);
        }
    }

    private Runnable buildQueueProcessor() {
        return () -> {
            while (isQueueEnabled) {

                try
                {
                    QueueTaskObject obj = mQueue.take();


                    if (obj != null) {
                        mLogger.debug("{} - Found new task (Queue size is: {})", Thread.currentThread().getName(), mQueue.size());
                        ExecuteQueueTask(obj);
                    }
                }
                catch (Exception ex)
                {

                }

            }
        };
    }

    private void ExecuteQueueTask(QueueTaskObject queueTaskObject) {
        try {
            queueTaskObject.getRunnable().run();
        } catch (Exception ex) {
            mLogger.error("{} - Error during executing task {} => {}", Thread.currentThread().getName(), queueTaskObject.getSender().getSimpleName(), ex.getMessage());
        }
    }

}
