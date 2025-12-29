package com.makebang.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.makebang.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 文件记录数据访问层
 */
@Mapper
public interface FileRecordRepository extends BaseMapper<FileRecord> {

    /**
     * 根据哈希值查找文件（用于秒传）
     */
    @Select("SELECT * FROM file_record WHERE hash = #{hash} AND status = 1 LIMIT 1")
    Optional<FileRecord> findByHash(@Param("hash") String hash);

    /**
     * 根据业务类型和业务ID查找文件
     */
    @Select("""
            SELECT * FROM file_record
            WHERE business_type = #{businessType}
            AND business_id = #{businessId}
            AND status = 1
            ORDER BY created_at DESC
            """)
    List<FileRecord> findByBusiness(@Param("businessType") String businessType,
                                     @Param("businessId") Long businessId);

    /**
     * 根据URL查找文件
     */
    @Select("SELECT * FROM file_record WHERE file_url = #{fileUrl} AND status = 1 LIMIT 1")
    Optional<FileRecord> findByUrl(@Param("fileUrl") String fileUrl);

    /**
     * 统计用户上传的文件大小总和
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM file_record WHERE user_id = #{userId} AND status = 1")
    long sumFileSizeByUserId(@Param("userId") Long userId);
}
