package com.github.tgiachi.cubemediaserver.interfaces.eventlistener;

public interface IEventListener<T extends Object> {
    void onEvent(T object);
}
