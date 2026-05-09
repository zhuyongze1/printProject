package com.print.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(page.getRecords());
        pr.setTotal(page.getTotal());
        pr.setPageNum(page.getCurrent());
        pr.setPageSize(page.getSize());
        pr.setPages(page.getPages());
        return pr;
    }
}
