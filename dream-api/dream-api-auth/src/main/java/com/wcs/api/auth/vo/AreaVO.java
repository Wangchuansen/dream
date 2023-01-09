package com.wcs.api.auth.vo;

import com.wcs.common.vo.BaseVO;

import java.util.List;

/**
 * 省市区地区信息VO
 *
 */
public class AreaVO extends BaseVO {
    private static final long serialVersionUID = 1L;

	private Long areaId;

	private String areaName;

	private Long parentId;

	private Integer level;

	private Integer check;

	/**
	 * 下级地址集合
	 */
	private List<AreaVO> areas;

	/**
	 * 下级地址的areaId
	 */
	private List<Long> areaIds;

	public Long getAreaId() {
		return areaId;
	}

	public Integer getCheck() {
		return check;
	}

	public void setCheck(Integer check) {
		this.check = check;
	}

	public List<AreaVO> getAreas() {
		return areas;
	}

	public void setAreas(List<AreaVO> areas) {
		this.areas = areas;
	}

	public List<Long> getAreaIds() {
		return areaIds;
	}

	public void setAreaIds(List<Long> areaIds) {
		this.areaIds = areaIds;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "AreaDTO{" +
				"areaId=" + areaId +
				", areaName='" + areaName + '\'' +
				", parentId=" + parentId +
				", level=" + level +
				", check=" + check +
				", areas=" + areas +
				", areaIds=" + areaIds +
				'}';
	}
}
