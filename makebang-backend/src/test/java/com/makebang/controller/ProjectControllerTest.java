package com.makebang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makebang.common.result.PageResult;
import com.makebang.dto.project.CreateProjectRequest;
import com.makebang.dto.project.ProjectQueryRequest;
import com.makebang.dto.project.UpdateProjectRequest;
import com.makebang.service.ProjectService;
import com.makebang.vo.CategoryVO;
import com.makebang.vo.ProjectVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController 单元测试
 */
@WebMvcTest(ProjectController.class)
@DisplayName("项目控制器测试")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private ProjectVO testProjectVO;
    private CreateProjectRequest createRequest;
    private UpdateProjectRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProjectVO = ProjectVO.builder()
                .id(1L)
                .title("测试项目")
                .description("这是一个测试项目")
                .categoryId(1)
                .budgetMin(new BigDecimal("1000"))
                .budgetMax(new BigDecimal("5000"))
                .status(1)
                .statusText("开放中")
                .userId(1L)
                .bidCount(5)
                .viewCount(100)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new CreateProjectRequest();
        createRequest.setTitle("新项目");
        createRequest.setDescription("新项目描述");
        createRequest.setCategoryId(1);
        createRequest.setBudgetMin(new BigDecimal("1000"));
        createRequest.setBudgetMax(new BigDecimal("5000"));

        updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("更新后的项目");
        updateRequest.setDescription("更新后的描述");
    }

    // ========== 需要登录的接口 ==========

    @Test
    @DisplayName("发布项目 - 成功")
    @WithMockUser(username = "testuser")
    void createProject_Success() throws Exception {
        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(testProjectVO);

        mockMvc.perform(post("/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试项目"));

        verify(projectService).createProject(any(CreateProjectRequest.class));
    }

    @Test
    @DisplayName("发布项目 - 未登录")
    void createProject_Unauthorized() throws Exception {
        mockMvc.perform(post("/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("发布项目 - 参数验证失败")
    @WithMockUser(username = "testuser")
    void createProject_ValidationFailed() throws Exception {
        CreateProjectRequest invalidRequest = new CreateProjectRequest();
        invalidRequest.setTitle(""); // 空标题

        mockMvc.perform(post("/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新项目 - 成功")
    @WithMockUser(username = "testuser")
    void updateProject_Success() throws Exception {
        when(projectService.updateProject(eq(1L), any(UpdateProjectRequest.class))).thenReturn(testProjectVO);

        mockMvc.perform(put("/v1/projects/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(projectService).updateProject(eq(1L), any(UpdateProjectRequest.class));
    }

    @Test
    @DisplayName("删除项目 - 成功")
    @WithMockUser(username = "testuser")
    void deleteProject_Success() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/v1/projects/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(projectService).deleteProject(1L);
    }

    @Test
    @DisplayName("发布项目(草稿->开放) - 成功")
    @WithMockUser(username = "testuser")
    void publishProject_Success() throws Exception {
        testProjectVO.setStatus(1);
        when(projectService.publishProject(1L)).thenReturn(testProjectVO);

        mockMvc.perform(post("/v1/projects/1/publish")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));

        verify(projectService).publishProject(1L);
    }

    @Test
    @DisplayName("关闭项目 - 成功")
    @WithMockUser(username = "testuser")
    void closeProject_Success() throws Exception {
        doNothing().when(projectService).closeProject(1L);

        mockMvc.perform(post("/v1/projects/1/close")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(projectService).closeProject(1L);
    }

    @Test
    @DisplayName("获取项目详情 - 成功")
    @WithMockUser(username = "testuser")
    void getProject_Success() throws Exception {
        when(projectService.getProjectDetail(1L)).thenReturn(testProjectVO);

        mockMvc.perform(get("/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试项目"));

        verify(projectService).getProjectDetail(1L);
    }

    @Test
    @DisplayName("分页查询项目 - 成功")
    @WithMockUser(username = "testuser")
    void queryProjects_Success() throws Exception {
        PageResult<ProjectVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testProjectVO));
        pageResult.setTotal(1L);
        pageResult.setCurrent(1);
        pageResult.setSize(10);

        when(projectService.queryProjects(any(ProjectQueryRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/v1/projects")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(projectService).queryProjects(any(ProjectQueryRequest.class));
    }

    @Test
    @DisplayName("获取我发布的项目 - 成功")
    @WithMockUser(username = "testuser")
    void getMyProjects_Success() throws Exception {
        PageResult<ProjectVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testProjectVO));
        pageResult.setTotal(1L);

        when(projectService.getMyProjects(any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/v1/projects/my")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(projectService).getMyProjects(any(), anyInt(), anyInt());
    }

    // ========== 公开接口(无需登录) ==========

    @Test
    @DisplayName("获取项目分类 - 公开接口")
    void getCategories_Success() throws Exception {
        List<CategoryVO> categories = Arrays.asList(
                CategoryVO.builder().id(1).name("Web开发").build(),
                CategoryVO.builder().id(2).name("移动开发").build()
        );

        when(projectService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/v1/projects/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Web开发"));

        verify(projectService).getAllCategories();
    }

    @Test
    @DisplayName("获取热门技能标签 - 公开接口")
    void getHotSkills_Success() throws Exception {
        List<String> skills = Arrays.asList("Java", "Python", "React", "Vue");

        when(projectService.getHotSkills()).thenReturn(skills);

        mockMvc.perform(get("/v1/projects/public/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("Java"));

        verify(projectService).getHotSkills();
    }

    @Test
    @DisplayName("公开查询项目列表 - 无需登录")
    void publicQueryProjects_Success() throws Exception {
        PageResult<ProjectVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(testProjectVO));
        pageResult.setTotal(1L);

        when(projectService.queryProjects(any(ProjectQueryRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/v1/projects/public/list")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(projectService).queryProjects(any(ProjectQueryRequest.class));
    }

    @Test
    @DisplayName("公开获取项目详情 - 无需登录")
    void publicGetProject_Success() throws Exception {
        when(projectService.getProjectDetail(1L)).thenReturn(testProjectVO);

        mockMvc.perform(get("/v1/projects/public/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试项目"));

        verify(projectService).getProjectDetail(1L);
    }
}
