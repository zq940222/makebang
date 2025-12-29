package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.ProjectEmbedding;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 项目向量数据访问层
 */
@Mapper
public interface ProjectEmbeddingRepository extends BaseMapper<ProjectEmbedding> {

    /**
     * 插入或更新项目向量
     */
    @Insert("""
            INSERT INTO project_embedding (project_id, embedding, content_hash, created_at, updated_at)
            VALUES (#{projectId}, #{embedding}::vector, #{contentHash}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (project_id)
            DO UPDATE SET
                embedding = #{embedding}::vector,
                content_hash = #{contentHash},
                updated_at = CURRENT_TIMESTAMP
            """)
    int upsertEmbedding(@Param("projectId") Long projectId,
                        @Param("embedding") String embedding,
                        @Param("contentHash") String contentHash);

    /**
     * 语义搜索 - 根据向量相似度查询项目ID
     * 使用余弦相似度（cosine similarity）
     */
    @Select("""
            SELECT project_id, 1 - (embedding <=> #{queryVector}::vector) as similarity
            FROM project_embedding
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> #{queryVector}::vector
            LIMIT #{limit}
            """)
    @Results({
            @Result(column = "project_id", property = "projectId"),
            @Result(column = "similarity", property = "similarity")
    })
    List<SimilarityResult> searchByVector(@Param("queryVector") String queryVector,
                                          @Param("limit") int limit);

    /**
     * 带阈值的语义搜索
     */
    @Select("""
            SELECT project_id, 1 - (embedding <=> #{queryVector}::vector) as similarity
            FROM project_embedding
            WHERE embedding IS NOT NULL
              AND 1 - (embedding <=> #{queryVector}::vector) >= #{threshold}
            ORDER BY embedding <=> #{queryVector}::vector
            LIMIT #{limit}
            """)
    @Results({
            @Result(column = "project_id", property = "projectId"),
            @Result(column = "similarity", property = "similarity")
    })
    List<SimilarityResult> searchByVectorWithThreshold(@Param("queryVector") String queryVector,
                                                        @Param("threshold") double threshold,
                                                        @Param("limit") int limit);

    /**
     * 根据项目ID获取内容哈希
     */
    @Select("SELECT content_hash FROM project_embedding WHERE project_id = #{projectId}")
    String getContentHash(@Param("projectId") Long projectId);

    /**
     * 删除项目向量
     */
    @Delete("DELETE FROM project_embedding WHERE project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 批量删除项目向量
     */
    @Delete("""
            <script>
            DELETE FROM project_embedding WHERE project_id IN
            <foreach collection="projectIds" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int deleteByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * 查询没有向量的项目ID
     */
    @Select("""
            SELECT p.id FROM project p
            LEFT JOIN project_embedding pe ON p.id = pe.project_id
            WHERE pe.id IS NULL AND p.status IN (1, 2)
            LIMIT #{limit}
            """)
    List<Long> findProjectsWithoutEmbedding(@Param("limit") int limit);

    /**
     * 相似度结果
     */
    class SimilarityResult {
        private Long projectId;
        private Double similarity;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(Double similarity) {
            this.similarity = similarity;
        }
    }
}
