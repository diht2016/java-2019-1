package ru.sbt.mipt.service;

import java.util.Date;
import java.util.List;

import ru.sbt.mipt.cache.*;

public interface Service {
    @Cache(cacheType = CacheType.IN_FILE, fileNamePrefix = "data", zip = true, identityBy = {true, true, false})
    List<String> run(String item, double value, Date date);
    @Cache(cacheType = CacheType.IN_MEMORY, listSize = 100_000)
    List<String> work(String item);
}
