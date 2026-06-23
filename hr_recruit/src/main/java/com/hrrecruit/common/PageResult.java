package com.hrrecruit.common;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页数据列表 */
    private List<T> records;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    public PageResult() {}

    public PageResult(long total, List<T> records, long current, long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;  // 计算总页数
    }
}
