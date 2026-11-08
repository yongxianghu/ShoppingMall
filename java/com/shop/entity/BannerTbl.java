package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String image;

    private String link;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdAt;
}
