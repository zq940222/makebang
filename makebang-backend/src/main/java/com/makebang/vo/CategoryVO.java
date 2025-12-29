package com.makebang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    private Integer id;

    private String name;

    private Integer parentId;

    private String icon;

    private Integer sort;

    /**
     * 子分类
     */
    private List<CategoryVO> children;
}
