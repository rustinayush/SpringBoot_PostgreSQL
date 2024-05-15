package com.example.MultiTenancy_Cache.services;

import com.example.MultiTenancy_Cache.CacheConfig;
import com.example.MultiTenancy_Cache.Db.entity.SchedulerCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class CacheServiceImpl implements CacheService {


    private CacheConfig cacheConfig;

    private final ObjectMapper objectMapper;

    List<Map<String, Object>> data ;


    @Autowired
    public CacheServiceImpl(CacheConfig cacheConfig, ObjectMapper objectMapper) {
        this.cacheConfig = cacheConfig;
        this.objectMapper = objectMapper;
    }

    private List<Map<String, Object>> loadDataFromJson(String cacheName) throws IOException {

        String path = getPathFromCacheConfig(cacheName);
        System.out.println("Path: " + path);

        // Read the JSON data from the file path
        List<Map<String, Object>> data  = objectMapper.readValue(new File(path),objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
        return data;
    }
    private String getPathFromCacheConfig(String cacheName) {
        return cacheConfig.getEntries().stream()
                .filter(entry -> entry.getCacheName().equals(cacheName))
                .findFirst()
                .map(entry -> entry.getPath())
                .orElseThrow(() -> new IllegalArgumentException("Cache name not found: " + cacheName));
    }

    private void writeDataToJson(String cacheName, List<Map<String, Object>> data) throws IOException {
        // Define the file path for the JSON file based on the cache name
        String filePath = getPathFromCacheConfig(cacheName);

        // Write the updated data to the JSON file
        objectMapper.writeValue(new File(filePath), data);
    }
    private String generateNewId(List<Map<String, Object>> list) {
        int maxId = 0;
        for (Map<String, Object> item : list) {
            String id = (String) item.get("id");
            if (id != null && id.matches("\\d+")) {
                int currentId = Integer.parseInt(id);
                if (currentId > maxId) {
                    maxId = currentId;
                }
            }
        }
        return String.valueOf(maxId + 1);
    }



    // Get the sublist based on adjusted start and end indices
    @Override
    public Page<Map<String, Object>> getAllData(String cacheName, Pageable pageable) throws IOException {
        List<Map<String, Object>> dataList = loadDataFromJson(cacheName);

        // Apply sorting if specified
        if (pageable.getSort().isSorted()) {
            // Sort the entire dataset based on the specified sort criteria
            dataList.sort(getComparatorFromSort(pageable.getSort()));
        }

        // If pageable is unpaged, return all data
        if (pageable.isUnpaged()) {
            return new PageImpl<>(dataList);
        }

        // Implement pagination
        int start;
        int end;

        if (pageable.getPageNumber() == 0) {
            // If page number is 0, start from the beginning
            start = 0;
            end = Math.min(pageable.getPageSize(), dataList.size());
        } else {
            // Adjust start index based on page number and size
            start = (pageable.getPageNumber() - 1) * pageable.getPageSize(); // Subtract 1 to adjust for 0-based index
            start = Math.min(start, dataList.size()); // Ensure it doesn't exceed the size of the data list
            end = Math.min((start + pageable.getPageSize()), dataList.size());
        }

        // Get the sublist based on adjusted start and end indices
        List<Map<String, Object>> paginatedList = dataList.subList(start, end);

        return new PageImpl<>(paginatedList, pageable, dataList.size());
    }




    private Comparator<Map<String, Object>> getComparatorFromSort(Sort sort) {
        List<Comparator<Map<String, Object>>> comparators = new ArrayList<>();
        for (Sort.Order order : sort) {
            Comparator<Map<String, Object>> comparator = getComparator(order.getProperty(), order.getDirection().toString());
            comparators.add(comparator);
        }

        return (m1, m2) -> {
            for (Comparator<Map<String, Object>> comparator : comparators) {
                int result = comparator.compare(m1, m2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }

    public static Comparator<Map<String, Object>> getComparator(String fieldName, String order) {
        Comparator<Map<String, Object>> comparator = (m1, m2) -> {
            try {
                Object value1 = m1.get(fieldName);
                Object value2 = m2.get(fieldName);

                if (value1 == null && value2 == null) {
                    return 0;
                } else if (value1 == null) {
                    return order.equalsIgnoreCase("asc") ? 1 : -1; // null is considered greater in ascending order
                } else if (value2 == null) {
                    return order.equalsIgnoreCase("asc") ? -1 : 1; // null is considered lesser in descending order
                }

                // Parse the id field as integer
                int id1 = Integer.parseInt(value1.toString());
                int id2 = Integer.parseInt(value2.toString());

                // Compare the ids
                int result = Integer.compare(id1, id2);
                return order.equalsIgnoreCase("asc") ? result : -result;
            } catch (NumberFormatException e) {
                // Handle parsing errors
                e.printStackTrace();
                throw new IllegalArgumentException("Field is not a number: " + fieldName);
            } catch (Exception e) {
                // Handle other errors
                e.printStackTrace();
                throw new RuntimeException("Error accessing field: " + fieldName, e);
            }
        };

        return comparator;
    }





    @Override
    public Map<String, Object> getDataById(String cacheName, String id) throws IOException {
       List<Map<String, Object>> data= loadDataFromJson(cacheName);
        for(Map<String, Object> entity : data){
            String EntityId=String.valueOf(entity.get("id"));
            if(EntityId.equals(id)){
                return entity;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> createData(String cacheName, Map<String, Object> newData) throws IOException {
        // Load existing data
        List<Map<String, Object>> allList = loadDataFromJson(cacheName);

        // Generate a new ID based on the existing IDs in the list
        String newId = generateNewId(allList);

        // Set the new ID in the newData map
        newData.put("id", newId);

        // Add the new data to the end of the list
        allList.add(newData);

        // Write the updated list back to the JSON file
        writeDataToJson(cacheName, allList);

        // Return the newly created data
        return newData;
    }



    @Override
    public Map<String,Object> updateData(String cacheName, Map<String, Object> newData, String id) throws IOException {
        // Load existing data
        List<Map<String, Object>> allList = loadDataFromJson(cacheName);

        // Find the item with the specified ID
        Optional<Map<String, Object>> optionalItem = allList.stream()
                .filter(item -> Objects.equals(item.get("id"), id))
                .findFirst();

        if (optionalItem.isPresent()) {
            // Update the item content
            Map<String, Object> existingItem = optionalItem.get();
            existingItem.putAll(newData);

            // Write the updated list back to the JSON file
            writeDataToJson(cacheName, allList);

            // Return the updated item
            return existingItem;
        } else {
            // Item with the specified ID not found
            throw new IllegalArgumentException("Item with ID " + id + " not found in cache " + cacheName);
        }
    }

    @Override
    public void deleteValue(String cacheName, String id) throws IOException {
        // Load existing data
        List<Map<String, Object>> allList = loadDataFromJson(cacheName);

        // Remove the item with the specified ID
        allList.removeIf(item -> Objects.equals(item.get("id"), id));

        // Write the updated list back to the JSON file
        writeDataToJson(cacheName, allList);
    }



    @Override
    public Page<Map<String, Object>> filterData(String cacheName, Map<String, String> searchAttributes) throws IOException {
        List<Map<String, Object>> dataList = loadDataFromJson(cacheName);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            for (Map<String, Object> data : dataList) {
                boolean matchAllAttributes = true;
                for (Map.Entry<String, String> entry : searchAttributes.entrySet()) {
                    String attributeName = entry.getKey();
                    String attributeValue = entry.getValue();

                    // Retrieve field dynamically using get() method
                    Object fieldValue = data.get(attributeName);

                    if (fieldValue == null || !fieldValue.toString().equalsIgnoreCase(attributeValue)) {
                        matchAllAttributes = false;
                        break; // Break out of inner loop if any attribute doesn't match
                    }
                }
                // Add data to result list only if all/exact attributes match
                if (matchAllAttributes) {
                    result.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching data", e);
        }

        // Return all filtered data without pagination
        return new PageImpl<>(result);
    }

    @Override
    public Page<Map<String, Object>> searchData(String cacheName, Map<String, String> searchAttributes) throws IOException {
        List<Map<String, Object>> dataList = loadDataFromJson(cacheName);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            for (Map<String, Object> data : dataList) {
                boolean matchPartialAttributes = true;
                for (Map.Entry<String, String> entry : searchAttributes.entrySet()) {
                    String attributeName = entry.getKey();
                    String attributeValue = entry.getValue();

                    // Retrieve field dynamically using get() method
                    Object fieldValue = data.get(attributeName);

                    if (fieldValue == null || !String.valueOf(fieldValue).contains(attributeValue)) {
                        matchPartialAttributes = false;
                        break; // Break out of inner loop if any attribute doesn't match partially
                    }
                }
                // Add data to result list only if all/partial attributes match
                if (matchPartialAttributes) {
                    result.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching data", e);
        }

        // Return all searched data without pagination
        return new PageImpl<>(result);
    }



}