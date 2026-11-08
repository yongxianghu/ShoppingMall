package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CategoryTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String icon;

    /** 0 表示一级分类 */
    private Integer parentId;

    private Integer sort;
}
