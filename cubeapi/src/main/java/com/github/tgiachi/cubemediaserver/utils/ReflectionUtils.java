package com.github.tgiachi.cubemediaserver.utils;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Utility class for get reflection
 */
public class ReflectionUtils {

    public static Set<Class<?>> getAnnotation(Class type) {
        return new Reflections("com.github").getTypesAnnotatedWith(type);
    }

    public static Set<Class<?>> getAnnotation(Class type, boolean scanJcl)
    {
        if (!scanJcl)
            return getAnnotation(type);
        //else
        //    return new Reflections(JclContext.get(), "com").getTypesAnnotatedWith(type);
        return null;
    }

    public static Set<Method> getMethods(Class<? extends Annotation> annotation)
    {
        return new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com")).addScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner())).getMethodsAnnotatedWith(annotation);
    }
}
