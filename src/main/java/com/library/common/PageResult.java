package com.library.common;

import lombok.Data;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public long getPages() {
        return (total + size - 1) / size;
    }
}
