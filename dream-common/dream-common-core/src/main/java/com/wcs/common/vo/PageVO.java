package com.wcs.common.vo;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    //总页数
    private Integer pages = 0;

    //总条目数
    private Long total = 0L;

    //结果集
    private List<T> list = new ArrayList<>();

    public PageVO(PageInfo<T> pageInfo) {
        this.pages = pageInfo.getPages();
        this.total = pageInfo.getTotal();
        this.list = pageInfo.getList();
    }
}
