package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.PageResult;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.project.CreateProjectRequest;
import com.makebang.dto.project.ProjectQueryRequest;
import com.makebang.dto.project.UpdateProjectRequest;
import com.makebang.entity.Category;
import com.makebang.entity.Project;
import com.makebang.entity.User;
import com.makebang.repository.CategoryRepository;
import com.makebang.repository.ProjectRepository;
import com.makebang.repository.UserRepository;
import com.makebang.service.ProjectService;
import com.makebang.service.UserService;
import com.makebang.vo.CategoryVO;
import com.makebang.vo.ProjectVO;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ProjectVO createProject(CreateProjectRequest request) {
        UserVO currentUser = userService.getCurrentUser();

        // 验证预算
        if (request.getBudgetMax().compareTo(request.getBudgetMin()) < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "最高预算不能低于最低预算");
        }

        // 创建项目
        Project project = new Project();
        project.setUserId(currentUser.getId());
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setCategoryId(request.getCategoryId());
        project.setBudgetMin(request.getBudgetMin());
        project.setBudgetMax(request.getBudgetMax());
        project.setDeadline(request.getDeadline());
        project.setSkillRequirements(request.getSkillRequirements());
        project.setAttachmentUrls(request.getAttachmentUrls());
        project.setStatus(Boolean.TRUE.equals(request.getDraft()) ? Project.Status.DRAFT.code : Project.Status.OPEN.code);
        project.setViewCount(0);
        project.setBidCount(0);

        projectRepository.insert(project);

        log.info("用户{}创建项目: {}", currentUser.getUsername(), project.getTitle());

        return toVO(project);
    }

    @Override
    @Transactional
    public ProjectVO updateProject(Long id, UpdateProjectRequest request) {
        Project project = getProjectEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证所有权
        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此项目");
        }

        // 只有草稿和开放状态的项目可以修改
        if (project.getStatus() != Project.Status.DRAFT.code && project.getStatus() != Project.Status.OPEN.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "当前状态不允许修改");
        }

        // 更新字段
        if (StringUtils.hasText(request.getTitle())) {
            project.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDescription())) {
            project.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            project.setCategoryId(request.getCategoryId());
        }
        if (request.getBudgetMin() != null) {
            project.setBudgetMin(request.getBudgetMin());
        }
        if (request.getBudgetMax() != null) {
            project.setBudgetMax(request.getBudgetMax());
        }
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }
        if (request.getSkillRequirements() != null) {
            project.setSkillRequirements(request.getSkillRequirements());
        }
        if (request.getAttachmentUrls() != null) {
            project.setAttachmentUrls(request.getAttachmentUrls());
        }

        projectRepository.updateById(project);

        return toVO(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此项目");
        }

        // 只有草稿状态可以删除
        if (project.getStatus() != Project.Status.DRAFT.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "只有草稿状态的项目可以删除");
        }

        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProjectVO publishProject(Long id) {
        Project project = getProjectEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此项目");
        }

        if (project.getStatus() != Project.Status.DRAFT.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "只有草稿状态可以发布");
        }

        project.setStatus(Project.Status.OPEN.code);
        projectRepository.updateById(project);

        return toVO(project);
    }

    @Override
    @Transactional
    public void closeProject(Long id) {
        Project project = getProjectEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此项目");
        }

        if (project.getStatus() != Project.Status.OPEN.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "只有开放中的项目可以关闭");
        }

        project.setStatus(Project.Status.CLOSED.code);
        projectRepository.updateById(project);
    }

    @Override
    public ProjectVO getProjectById(Long id) {
        return toVO(getProjectEntity(id));
    }

    @Override
    @Transactional
    public ProjectVO getProjectDetail(Long id) {
        Project project = getProjectEntity(id);

        // 增加浏览量
        projectRepository.incrementViewCount(id);
        project.setViewCount(project.getViewCount() + 1);

        return toVO(project);
    }

    @Override
    public PageResult<ProjectVO> queryProjects(ProjectQueryRequest request) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // 基础条件
        wrapper.isNull(Project::getDeletedAt);

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(Project::getTitle, request.getKeyword())
                    .or()
                    .like(Project::getDescription, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Project::getCategoryId, request.getCategoryId());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Project::getStatus, request.getStatus());
        } else {
            // 默认只查询开放中的项目
            wrapper.eq(Project::getStatus, Project.Status.OPEN.code);
        }

        // 用户筛选
        if (request.getUserId() != null) {
            wrapper.eq(Project::getUserId, request.getUserId());
        }

        // 预算筛选
        if (request.getBudgetMinFrom() != null) {
            wrapper.ge(Project::getBudgetMin, request.getBudgetMinFrom());
        }
        if (request.getBudgetMinTo() != null) {
            wrapper.le(Project::getBudgetMin, request.getBudgetMinTo());
        }

        // 排序
        String sortBy = request.getSortBy();
        boolean isAsc = "asc".equalsIgnoreCase(request.getSortOrder());
        switch (sortBy) {
            case "budget_max" -> wrapper.orderBy(true, isAsc, Project::getBudgetMax);
            case "view_count" -> wrapper.orderBy(true, isAsc, Project::getViewCount);
            case "bid_count" -> wrapper.orderBy(true, isAsc, Project::getBidCount);
            default -> wrapper.orderBy(true, isAsc, Project::getCreatedAt);
        }

        // 分页查询
        Page<Project> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<Project> result = projectRepository.selectPage(page, wrapper);

        // 转换为VO
        List<ProjectVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public PageResult<ProjectVO> getMyProjects(Integer status, Integer current, Integer size) {
        UserVO currentUser = userService.getCurrentUser();

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getUserId, currentUser.getId())
                .isNull(Project::getDeletedAt)
                .orderByDesc(Project::getCreatedAt);

        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }

        Page<Project> page = new Page<>(current, size);
        IPage<Project> result = projectRepository.selectPage(page, wrapper);

        List<ProjectVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();

        // 构建树形结构
        Map<Integer, List<Category>> groupByParent = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        return buildCategoryTree(groupByParent, 0);
    }

    @Override
    public List<String> getHotSkills() {
        // 返回热门技能标签
        return Arrays.asList(
                "React", "Vue", "Angular", "TypeScript", "JavaScript",
                "Node.js", "Python", "Java", "Go", "PHP",
                "MySQL", "PostgreSQL", "MongoDB", "Redis",
                "微信小程序", "React Native", "Flutter",
                "Spring Boot", "Django", "FastAPI",
                "Docker", "Kubernetes", "AWS"
        );
    }

    /**
     * 获取项目实体
     */
    private Project getProjectEntity(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null || project.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    /**
     * 构建分类树
     */
    private List<CategoryVO> buildCategoryTree(Map<Integer, List<Category>> groupByParent, Integer parentId) {
        List<Category> children = groupByParent.get(parentId);
        if (children == null) {
            return Collections.emptyList();
        }

        return children.stream().map(category -> {
            CategoryVO vo = CategoryVO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .parentId(category.getParentId())
                    .icon(category.getIcon())
                    .sort(category.getSort())
                    .children(buildCategoryTree(groupByParent, category.getId()))
                    .build();
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 实体转VO
     */
    private ProjectVO toVO(Project project) {
        if (project == null) return null;

        ProjectVO vo = ProjectVO.builder()
                .id(project.getId())
                .userId(project.getUserId())
                .title(project.getTitle())
                .description(project.getDescription())
                .categoryId(project.getCategoryId())
                .budgetMin(project.getBudgetMin())
                .budgetMax(project.getBudgetMax())
                .deadline(project.getDeadline())
                .skillRequirements(project.getSkillRequirements())
                .attachmentUrls(project.getAttachmentUrls())
                .status(project.getStatus())
                .viewCount(project.getViewCount())
                .bidCount(project.getBidCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();

        // 加载分类名称
        if (project.getCategoryId() != null) {
            Category category = categoryRepository.selectById(project.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 加载用户信息
        User user = userRepository.selectById(project.getUserId());
        if (user != null) {
            vo.setUser(userService.toVO(user));
        }

        return vo;
    }
}
