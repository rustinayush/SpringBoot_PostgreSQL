package com.example.MultiTenancy_Cache.Db.repository;

import com.example.MultiTenancy_Cache.Db.entity.SchedulerCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheRepository extends JpaRepository<SchedulerCache, String> {

}
