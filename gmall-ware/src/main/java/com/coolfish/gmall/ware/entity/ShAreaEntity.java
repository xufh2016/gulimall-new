package com.coolfish.gmall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 全国省市区信息
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:22:16
 */
@Data
@TableName("wms_sh_area")
public class ShAreaEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId
	private Integer id;
	/**
	 * 父id
	 */
	private Integer pid;
	/**
	 * 简称
	 */
	private String shortname;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 全称
	 */
	private String mergerName;
	/**
	 * 层级 0 1 2 省市区县
	 */
	private Integer level;
	/**
	 * 拼音
	 */
	private String pinyin;
	/**
	 * 长途区号
	 */
	private String code;
	/**
	 * 邮编
	 */
	private String zipCode;
	/**
	 * 首字母
	 */
	private String first;
	/**
	 * 经度
	 */
	private String lng;
	/**
	 * 纬度
	 */
	private String lat;

}
