package com.example.MultiTenancy_Cache;

import com.example.MultiTenancy_Cache.Db.entity.SchedulerCache;
import com.example.MultiTenancy_Cache.Db.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "cache")
public class CacheConfig {

    private List<SchedulerCache> entries;
    private final CacheRepository cacheRepository;



    @Autowired
    public CacheConfig(@Value("${config.path}") String value, CacheRepository cacheRepository){
        this.cacheRepository = cacheRepository;
        this.entries = new ArrayList<>();

        List<SchedulerCache> all = cacheRepository.findAll();
        all.forEach(schedulerCache -> {
            addCacheEntry( schedulerCache.getCacheName(), schedulerCache.getPath(), schedulerCache.getFileSystem(), schedulerCache.getOrgId());
        });


    }

    private void addCacheEntry(String cacheName, String path, String fileSystem , String orgId) {
        SchedulerCache cacheEntry = new SchedulerCache();
        cacheEntry.setCacheName(cacheName);
        cacheEntry.setPath(path);
        cacheEntry.setFileSystem(fileSystem);
        cacheEntry.setOrgId(orgId);
        entries.add(cacheEntry);
        System.out.println(entries);
    }



    public List<SchedulerCache> getEntries() {
        System.out.println(entries);
        return entries;
    }


}
