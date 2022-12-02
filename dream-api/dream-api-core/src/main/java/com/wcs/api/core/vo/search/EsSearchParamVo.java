package com.wcs.api.core.vo.search;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wcs.common.dto.PageDTO.MAX_PAGE_SIZE;

/***
 * 首页搜索条件
 * @author wcs
 */
@Data
public class EsSearchParamVo implements Serializable {
    private static final long serialVersionUID = 3036631164479759841L;

    /**搜索关键词*/
    private String name;
    /**关键词对应的标签id*/
    private String nameLabel;
    /**name分词后集合*/
    Set<String>nameStrSet = new HashSet<>();
    /**标签，分割*/
    private String labelIds;
    /**0 时间降序 1 时间升序*/
    private Integer createTimeSort;
    /**0 浏览量降序 1 浏览量升序*/
    private Integer viewSort;
    /** 0 人员规模降序 1 人员规模升序 */
    private Integer peopleNumberSort;
    /**结束时间排序 0 时间降序 1 时间升序*/
    private Integer endTimeSort;
    /**金额 0 降序 1 升序*/
    private Integer budgetSort;
    /**置顶 1 置顶降序*/
    private Integer isTopSort;
    /**是否为历史 0 否 1是*/
    private Integer isHistory;
    /**是否随机排序 0 否 1是 */
    private Integer isRandomSort;
    /**是否为审核数据 0 否 1是*/
    private Integer isApply;
    /**不包含id*/
    private List<String> noIds = new ArrayList<>();
    /**显示类型 */
    List<Integer>showTypeIds = new ArrayList<>();
    /**不显示类型 */
    List<Integer>noShowTypeIds = new ArrayList<>();
    /**不显示类型 */
    List<Integer>noApplyStatus = new ArrayList<>();
    /**显示类型 */
    List<Integer>showApplyStatus = new ArrayList<>();
    /**模块id 0、全部 1、案例 、2、方案、3、场景、4、白皮书、5、政策、8、商机、9、ISV*/
    private Integer moduleId;
    /**模块id*/
    private List<Integer>moduleIdList ;
    /**来源类型 0 正常添加 1导入*/
    private Integer sourceType;

    /**
     * 当前页
     */
    @NotNull(message = "pageIndex 不能为空")
    private Integer pageIndex = 1;

    @NotNull(message = "pageSize 不能为空")
    private Integer pageSize = 10;

    public Integer getPageNum() {
        return pageIndex;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
            return;
        }
        this.pageSize = pageSize;
    }
}
