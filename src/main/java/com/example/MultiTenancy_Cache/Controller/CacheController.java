package com.example.MultiTenancy_Cache.Controller;



import com.example.MultiTenancy_Cache.services.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CacheController {




    private final CacheService cacheService;

    private final ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    public CacheController( CacheService cacheService, ObjectMapper objectMapper) {
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }


    @GetMapping("/{orgid}/{cacheName}")
    public Page<Map<String, Object>> getAllData(
            @PathVariable String orgid,
            @PathVariable String cacheName,
            @RequestParam(value = "pageNumber", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam Map<String, String> searchData,
            @RequestParam Map<String, String> filter
    ) throws IOException {
        Pageable pageable = null;
        Sort sort = null;

        // Step 1: Specific search (like searching by ID)
        if (!searchData.isEmpty()) {
            Page<Map<String, Object>> specificSearchResult = cacheService.searchData(orgid,cacheName, searchData);
            if (!specificSearchResult.isEmpty()) {
                return specificSearchResult; // Return specific search result if found
            }
        }

        // Step 2: Generic or partial searching
        if (!filter.isEmpty()) {
            Page<Map<String, Object>> partialSearchResult = cacheService.filterData(orgid, cacheName, filter);
            if (!partialSearchResult.isEmpty()) {
                return partialSearchResult; // Return partial search result if found
            }
        }

        // Step 3: Sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        }

        // Step 4: Pagination
        if (page != null && size != null) {
            if (sort != null) {
                pageable = PageRequest.of(page, size, sort);
            } else {
                pageable = PageRequest.of(page, size); // Create pageable object without sorting
            }
        } else if (sort != null) {
            // If only sorting is provided, default to first page with maximum size
            pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
        } else {
            // If no pagination or sorting parameters are provided, return all data
            return cacheService.getAllData(orgid, cacheName, Pageable.unpaged());
        }

        return cacheService.getAllData(orgid, cacheName, pageable);
    }


    @GetMapping("/{orgid}/{cacheName}/{id}")
    public Map<String, Object> getDataById(@PathVariable String orgid,@PathVariable String cacheName, @PathVariable String id) throws IOException {
        return cacheService.getDataById(orgid, cacheName,id);
    }

    @PostMapping("/{orgid}/{cacheName}")
    public Map<String,Object> createData(@PathVariable String orgid,@PathVariable String cacheName,@RequestBody Map<String,Object>  data) throws IOException {
          return cacheService.createData(orgid, cacheName,data);
    }

    @PutMapping("/{orgid}/{cacheName}/{id}")
    public Map<String,Object> updateData(@PathVariable String orgid,@PathVariable String cacheName, @RequestBody Map<String,Object> data, @PathVariable String id) throws IOException {
        return cacheService.updateData(orgid, cacheName, data,id);
    }

    @DeleteMapping("/{orgid}/{cacheName}/{id}")
    public String deleteValue(@PathVariable String orgid,@PathVariable String cacheName, @PathVariable String id) throws IOException {
        cacheService.deleteValue(orgid, cacheName,id);
           return "Deleted SuccessFully";
    }


}

