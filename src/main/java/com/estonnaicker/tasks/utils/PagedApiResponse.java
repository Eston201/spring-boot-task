package com.estonnaicker.tasks.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class PagedApiResponse<T, K> extends ApiResponse<T> {
    private final Map<String, Object> metadata = new HashMap<>();    

    public PagedApiResponse(T data, Page<K> page) {
        super(data);
        this.buildPagedMetaData(page);
    }

    private void buildPagedMetaData(Page<K> page) {
        metadata.put("pageNumber", page.getNumber());
        metadata.put("pageSize", page.getSize());
        metadata.put("totalElements", page.getTotalElements());
        metadata.put("totalPages", page.getTotalPages());
        metadata.put("last", page.isLast());
    }
}
