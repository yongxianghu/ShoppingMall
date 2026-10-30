package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String receiver;

    private String tel;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    /** 0 否 1 是 */
    private Integer isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
