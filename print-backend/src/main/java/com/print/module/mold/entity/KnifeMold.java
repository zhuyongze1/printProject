package com.print.module.mold.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_knife_mold")
public class KnifeMold {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String moldNo;
    private String moldName;
    private String shapeType;
    private Double length;
    private Double width;
    private Double diameter;
    private String model;
    private String areaCode;
    private String shelfNo;
    private String layerNo;
    private String positionNo;
    private String locationCode;
    private Integer status;
    private String remark;

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
