package com.makebang.service;

import com.makebang.common.result.PageResult;
import com.makebang.dto.project.CreateProjectRequest;
import com.makebang.dto.project.ProjectQueryRequest;
import com.makebang.dto.project.UpdateProjectRequest;
import com.makebang.vo.CategoryVO;
import com.makebang.vo.ProjectVO;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 创建项目
     */
    ProjectVO createProject(CreateProjectRequest request);

    /**
     * 更新项目
     */
    ProjectVO updateProject(Long id, UpdateProjectRequest request);

    /**
     * 删除项目
     */
    void deleteProject(Long id);

    /**
     * 发布项目(草稿->开放)
     */
    ProjectVO publishProject(Long id);

    /**
     * 关闭项目
     */
    void closeProject(Long id);

    /**
     * 获取项目详情
     */
    ProjectVO getProjectById(Long id);

    /**
     * 获取项目详情(增加浏览量)
     */
    ProjectVO getProjectDetail(Long id);

    /**
     * 分页查询项目
     */
    PageResult<ProjectVO> queryProjects(ProjectQueryRequest request);

    /**
     * 获取当前用户的项目列表
     */
    PageResult<ProjectVO> getMyProjects(Integer status, Integer current, Integer size);

    /**
     * 获取所有分类(树形结构)
     */
    List<CategoryVO> getAllCategories();

    /**
     * 获取热门技能标签
     */
    List<String> getHotSkills();
}
