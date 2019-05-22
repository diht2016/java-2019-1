package ru.edhunter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class PluginManager {
    private final String pluginRootDirectory;

    public PluginManager(String pluginRootDirectory) {
        this.pluginRootDirectory = pluginRootDirectory;
    }

    public Plugin load(String pluginName, String pluginClassName)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, MalformedURLException {

        String pluginPath = pluginRootDirectory + pluginName;
        URL[] urls = {new File(pluginPath).toURL()};
        ClassLoader cl = new PluginClassLoader(urls);
        return (Plugin) cl.loadClass(pluginClassName).newInstance();
    }
}
