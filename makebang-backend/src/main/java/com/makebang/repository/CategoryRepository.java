package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类数据访问接口
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {

    /**
     * 获取所有分类(按排序)
     */
    @Select("SELECT * FROM category WHERE deleted_at IS NULL ORDER BY sort ASC, id ASC")
    List<Category> findAll();

    /**
     * 获取一级分类
     */
    @Select("SELECT * FROM category WHERE parent_id = 0 AND deleted_at IS NULL ORDER BY sort ASC, id ASC")
    List<Category> findRootCategories();

    /**
     * 获取子分类
     */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND deleted_at IS NULL ORDER BY sort ASC, id ASC")
    List<Category> findByParentId(@Param("parentId") Integer parentId);
}
