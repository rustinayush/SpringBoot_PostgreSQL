package com.example.MultiTenancy_Cache.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Map;

public interface CacheService {

    Page<Map<String, Object>> getAllData(String orgid, String cacheName, Pageable pageable) throws IOException;

    Map<String, Object> getDataById(String orgid, String cacheName, String id) throws IOException;

    Map<String, Object> createData(String orgid, String cacheName, Map<String, Object> data) throws IOException;

    Map<String, Object> updateData(String orgid, String cacheName, Map<String, Object> data, String id) throws IOException;

    void deleteValue(String orgid, String cacheName, String id) throws IOException;

    Page<Map<String, Object>> filterData(String orgid, String cacheName, Map<String, String> searchAttributes) throws IOException;

    Page<Map<String, Object>> searchData(String orgid, String cacheName, Map<String, String> searchAttributes) throws IOException;
}
