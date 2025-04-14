package com.estonnaicker.tasks.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class PagedApiResponse<T> extends ApiResponse<List<T>> {

    @ApiModelProperty(
        value = "Pagination metadata (page number, size, total elements, etc.)",
        position = 2,
        dataType = "Map",
        example = "{\"pageNumber\": 0, \"pageSize\": 10, \"totalElements\": 50, \"totalPages\": 5, \"last\": false}"
    )
    private final Map<String, Object> metadata = new HashMap<>();    

    public PagedApiResponse(List<T> data, Page<T> page) {
        super(data);
        this.buildPagedMetaData(page);
    }

    private void buildPagedMetaData(Page<T> page) {
        metadata.put("pageNumber", page.getNumber());
        metadata.put("pageSize", page.getSize());
        metadata.put("totalElements", page.getTotalElements());
        metadata.put("totalPages", page.getTotalPages());
        metadata.put("last", page.isLast());
    }
}
