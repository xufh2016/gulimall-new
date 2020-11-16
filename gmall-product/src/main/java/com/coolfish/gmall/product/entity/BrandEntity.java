package com.coolfish.gmall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.coolfish.common.valid.AddGroup;
import com.coolfish.common.valid.ListValue;
import com.coolfish.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 18:41:42
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty(message = "不能为空", groups = {AddGroup.class})
    @URL(message = "Logo必须是一个合法的Url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull
    @ListValue(vals = {0, 1})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个英文字母")
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull
    @Min(value = 0, message = "排序必须大于等于0")
    private Integer sort;

}
