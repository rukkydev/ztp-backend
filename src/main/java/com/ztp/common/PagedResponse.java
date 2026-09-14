package com.ztp.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedResponse<T> {
    private final List<T> data;
    private final long total;
    private final int page;
    private final int pageSize;
}