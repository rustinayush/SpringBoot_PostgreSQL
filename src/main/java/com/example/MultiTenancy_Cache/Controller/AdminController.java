package com.example.MultiTenancy_Cache.Controller;

import com.example.MultiTenancy_Cache.Db.entity.SchedulerCache;
import com.example.MultiTenancy_Cache.Db.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private final CacheRepository cacheRepository;

    public AdminController(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @PostMapping("/Cache")
    public SchedulerCache createCache(@RequestBody SchedulerCache cacheInfo) {
        return cacheRepository.save(cacheInfo);
    }
//    @PutMapping("/Cache/{id}")
//    public ResponseEntity<SchedulerCache> updateCache(@PathVariable Long id, @RequestBody SchedulerCache cacheDetails){
//        SchedulerCache cache = cacheRepository.findById(id);
//
//    }


}
