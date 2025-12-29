package com.makebang.controller;

import com.makebang.common.result.PageResult;
import com.makebang.common.result.Result;
import com.makebang.dto.project.CreateProjectRequest;
import com.makebang.dto.project.ProjectQueryRequest;
import com.makebang.dto.project.UpdateProjectRequest;
import com.makebang.service.ProjectService;
import com.makebang.vo.CategoryVO;
import com.makebang.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目控制器
 */
@Tag(name = "项目管理", description = "项目需求的发布、查询、管理等接口")
@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "发布项目")
    @PostMapping
    public Result<ProjectVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return Result.success(projectService.createProject(request));
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return Result.success(projectService.updateProject(id, request));
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @Operation(summary = "发布项目(草稿->开放)")
    @PostMapping("/{id}/publish")
    public Result<ProjectVO> publishProject(@PathVariable Long id) {
        return Result.success(projectService.publishProject(id));
    }

    @Operation(summary = "关闭项目")
    @PostMapping("/{id}/close")
    public Result<Void> closeProject(@PathVariable Long id) {
        projectService.closeProject(id);
        return Result.success();
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<ProjectVO> getProject(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }

    @Operation(summary = "分页查询项目")
    @GetMapping
    public Result<PageResult<ProjectVO>> queryProjects(ProjectQueryRequest request) {
        return Result.success(projectService.queryProjects(request));
    }

    @Operation(summary = "获取我发布的项目")
    @GetMapping("/my")
    public Result<PageResult<ProjectVO>> getMyProjects(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(projectService.getMyProjects(status, current, size));
    }

    // ========== 公开接口(无需登录) ==========

    @Operation(summary = "获取项目分类")
    @GetMapping("/public/categories")
    public Result<List<CategoryVO>> getCategories() {
        return Result.success(projectService.getAllCategories());
    }

    @Operation(summary = "获取热门技能标签")
    @GetMapping("/public/skills")
    public Result<List<String>> getHotSkills() {
        return Result.success(projectService.getHotSkills());
    }

    @Operation(summary = "公开查询项目列表")
    @GetMapping("/public/list")
    public Result<PageResult<ProjectVO>> publicQueryProjects(ProjectQueryRequest request) {
        // 强制只查询开放中的项目
        request.setStatus(1);
        return Result.success(projectService.queryProjects(request));
    }

    @Operation(summary = "公开获取项目详情")
    @GetMapping("/public/{id}")
    public Result<ProjectVO> publicGetProject(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }
}
