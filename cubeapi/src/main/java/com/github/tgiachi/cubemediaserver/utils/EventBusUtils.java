package com.github.tgiachi.cubemediaserver.utils;

import com.google.common.eventbus.EventBus;

public class EventBusUtils {

    private static EventBusUtils mInstance;

    public static EventBusUtils getSingleton()
    {
        if (mInstance == null)
            mInstance = new EventBusUtils();

        return mInstance;
    }

    private final EventBus mEventBus;


    public EventBusUtils()
    {
        mEventBus = new EventBus();
    }


    public void register(Object obj)
    {
        mEventBus.register(obj);
    }


    public void broadcast(Object obj)
    {
        mEventBus.post(obj);
    }

    public void unregister(Object obj)
    {
        mEventBus.unregister(obj);
    }
}


