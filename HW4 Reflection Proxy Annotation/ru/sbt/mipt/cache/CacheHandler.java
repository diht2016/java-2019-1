package ru.sbt.mipt.cache;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CacheHandler<T> implements InvocationHandler {
    private final T object;
    private final String cachePath;
    private final Map<Method, Map<Object[], Object>> storage = new HashMap<>();

    CacheHandler(T object, String cachePath) {
        this.cachePath = cachePath;
        this.object = object;
        Class objectClass = object.getClass();
        for (Method method : objectClass.getDeclaredMethods()) {
            Cache annotation = method.getAnnotation(Cache.class);
            if (annotation != null) {
                Map<Object[], Object> cache = new HashMap<>();

                if (annotation.cacheType() == CacheType.IN_FILE) {
                    // try to load from file
                    String filePath = getFilePath(method);
                    File file = new File(filePath);
                    if (file.exists() && !file.isDirectory()) {
                        try (
                                FileInputStream fIn = new FileInputStream(filePath);
                                InputStream zIn = annotation.zip() ? new ZipInputStream(fIn) : fIn;
                                ObjectInputStream oIn = new ObjectInputStream(zIn)
                        ) {
                            @SuppressWarnings("unchecked")
                            Map<Object[], Object> loadedCache = (Map<Object[], Object>) oIn.readObject();
                            cache = loadedCache;
                        } catch (IOException | ClassNotFoundException | ClassCastException err) {
                            err.printStackTrace();
                        }
                    }
                }

                storage.put(method, cache);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<Object[], Object> cache = storage.get(method);
        if (cache == null) {
            // skip method if not marked as cached
            return method.invoke(object, args);
        }
        Cache annotation = method.getAnnotation(Cache.class);
        Object[] filteredArgs = filterArgs(args, annotation.identityBy());
        if (cache.containsKey(filteredArgs)) {
            return cache.get(filteredArgs);
        }

        Object result = method.invoke(object, args);

        if (result instanceof List) {
            List resultList = (List) result;
            // don't cache if list is too big
            if (resultList.size() > annotation.listSize()) {
                return result;
            }
        }
        cache.put(filteredArgs, result);
        if (annotation.cacheType() == CacheType.IN_FILE) {
            try (
                FileOutputStream fOut = new FileOutputStream(getFilePath(method));
                OutputStream zOut = annotation.zip() ? new ZipOutputStream(fOut) : fOut;
                ObjectOutputStream oOut = new ObjectOutputStream(zOut)
            ) {
                oOut.writeObject(cache);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
        return result;
    }

    private String getFilePath(Method method) {
        Cache annotation = method.getAnnotation(Cache.class);
        String prefix = annotation.fileNamePrefix();
        if (prefix.equals("")) {
            prefix = method.getName();
        }
        String fileName = prefix + "_" + object.getClass().getName() + ".dat";
        return cachePath + fileName;
    }

    private Object[] filterArgs(Object[] args, boolean[] identityBy) {
        if (identityBy.length == 0) {
            return args;
        }
        int count = 0;
        for (boolean b : identityBy) {
            if (b) {
                count++;
            }
        }
        Object[] filteredArgs = new Object[count];
        count = 0;
        int pos = 0;
        for (boolean b : identityBy) {
            if (b) {
                filteredArgs[count] = args[pos];
                count++;
            }
            pos++;
        }
        return filteredArgs;
    }
}
