package ru.edhunter;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {
    public PluginClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String pluginClassName, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findClass(pluginClassName);
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}
