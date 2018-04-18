package com.github.tgiachi.cubemediaserver.services;

import com.github.tgiachi.cubemediaserver.annotations.MediaEventListener;
import com.github.tgiachi.cubemediaserver.interfaces.eventlistener.IEventListener;
import com.github.tgiachi.cubemediaserver.interfaces.services.IEventListenerService;
import com.github.tgiachi.cubemediaserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class EventListenerService implements IEventListenerService {

    private Logger mLogger = LoggerFactory.getLogger(EventListenerService.class);

    private HashMap<Class<?>, List<Class<?>>> mListeners = new HashMap<>();

    @Autowired
    public ApplicationContext mApplicationContext;


    @PostConstruct
    public void init() {
        mLogger.info("Scanning for event listeners...");

        try {
            Set<Class<?>> types = ReflectionUtils.getAnnotation(MediaEventListener.class);
            mLogger.info("Found {} eventListeners", types.size());

            types.forEach(t -> addEventListener(t));

        } catch (Exception ex) {

        }
    }


    @Override
    public void publishEvent(Object obj)
    {
        if (mListeners.containsKey(obj.getClass()))
        {
            List<Class<?>> classes = mListeners.get(obj.getClass());

            classes.parallelStream().forEach(s -> {

                IEventListener listener = (IEventListener)mApplicationContext.getBean(s);

                listener.onEvent(obj);

            });

        }


    }

    private void addEventListener(Class<?> classz) {
        try {

            MediaEventListener ann = classz.getAnnotation(MediaEventListener.class);

            for (Class<?> c : ann.eventClasses()) {
               // if (IEventListener.class.isAssignableFrom(c)) {

                    if (!mListeners.containsKey(c))
                        mListeners.put(c, new ArrayList<>());

                    mListeners.get(c).add(classz);
                    mLogger.info("Registering class {} for event {}", classz.getSimpleName(), c.getSimpleName());
           //     }
            }

            registerBean(classz);

        } catch (Exception ex) {
            mLogger.error("Error during init eventListener {} => {}", classz.getSimpleName(), ex);
        }
    }

    private void registerBean(Class<?> classz) {

        //if (((GenericWebApplicationContext) mApplicationContext).co)
        ((GenericWebApplicationContext) mApplicationContext).registerBean(classz);
    }

}
