package com.myproject.demo.dbtest.vo;

import lombok.Data;

import java.util.List;

@Data
public class NumberVO {
    /**
     * 最大值
     */
    private Integer maxNum;

    /**
     * 并联终点
     */
    private String parallelEndId;

    /**
     * 并联最终终点
     */
    private String parallelEndIdFinal;

    /**
     * 并联最终终点的位置
     */
    private Integer endIndex;

    /**
     * 并联终点结果集
     */
    private String parallelEndIds;

    /**
     * 并联节点集合
     */
    private List<String> parallelSourceIds;

    /**
     * 并联终点集合
     */
    private List<String> parallelTargetIds;

    public NumberVO() {
    }

    public NumberVO(Integer maxNum) {
        this.maxNum = maxNum;
    }
}
