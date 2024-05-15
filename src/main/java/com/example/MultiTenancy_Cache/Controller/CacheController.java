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


    @GetMapping("/{cacheName}")
    public Page<Map<String, Object>> getAllData(
            @PathVariable String cacheName,
            @RequestParam(value = "pageNumber", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam Map<String, String> searchData,
            @RequestParam Map<String, String> filter
    ) throws IOException {
        Pageable pageable = null;

        if (page != null && size != null) {
            // Create pageable object for pagination if page and size parameters are provided
            pageable = PageRequest.of(page, size);
        }

        // Step 1: Specific search (like searching by ID)
        if (!searchData.isEmpty()) {
            Page<Map<String, Object>> specificSearchResult = cacheService.searchData(cacheName, searchData);
            if (!specificSearchResult.isEmpty()) {
                return specificSearchResult; // Return specific search result if found
            }
        }

        // Step 2: Generic or partial searching
        if (!filter.isEmpty()) {
            Page<Map<String, Object>> partialSearchResult = cacheService.filterData(cacheName, filter);
            if (!partialSearchResult.isEmpty()) {
                return partialSearchResult; // Return partial search result if found
            }
        }

        // Step 3: Sorting
        Sort sort = null;
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        }

        // Step 4: Pagination
        if (pageable == null && sort != null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, sort); // Set default pagination if not provided
        } else if (pageable == null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE); // Set default pagination if not provided
        }

        // If no pagination and sorting parameters are provided, return all data
        if (pageable.getPageNumber() == 0 && pageable.getPageSize() == Integer.MAX_VALUE && sort == null) {
            return cacheService.getAllData(cacheName, Pageable.unpaged());
        }

        return cacheService.getAllData(cacheName, pageable);
    }


    private boolean containsGeneralParams(Map<String, String> requestParams) {
        // Check if any of the general params are present in the requestParams map
        return requestParams.containsKey("sortBy")
                || requestParams.containsKey("pageSize")
                || requestParams.containsKey("pageNumber")
                || requestParams.containsKey("sortOrder");
    }


    @GetMapping("/{cacheName}/{id}")
    public Map<String, Object> getDataById(@PathVariable String cacheName, @PathVariable String id) throws IOException {
        return cacheService.getDataById(cacheName,id);
    }

    @PostMapping("/{cacheName}")
    public Map<String,Object> createData(@PathVariable String cacheName,@RequestBody Map<String,Object>  data) throws IOException {
          return cacheService.createData(cacheName,data);
    }

    @PutMapping("/{cacheName}/{id}")
    public Map<String,Object> updateData(@PathVariable String cacheName, @RequestBody Map<String,Object> data, @PathVariable String id) throws IOException {
        return cacheService.updateData(cacheName, data,id);
    }

    @DeleteMapping("{cacheName}/{id}")
    public String deleteValue(@PathVariable String cacheName, @PathVariable String id) throws IOException {
        cacheService.deleteValue(cacheName,id);
           return "Deleted SuccessFully";
    }


}

