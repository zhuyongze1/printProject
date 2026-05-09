package com.print.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private LocalDate orderDate;
    private String deliveryNo;
    private String printName;
    private Integer quantity;
    private Double unitPrice;
    private Double amount;
    private String scheduleNo;
    private String material;
    private Long customerId;
    private String customerName;
    private Long moldId;
    private String moldName;
    private String remark;
    private Integer shipped;
    private LocalDate deliveryDate;
    private String extraInfo;
    private String reserveField;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long createBy;
    private Long updateBy;

    @TableLogic
    @TableField(select = false)
    private Integer isDeleted;
}
