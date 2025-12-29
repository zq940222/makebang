package com.makebang.service;

import com.makebang.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file         文件
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @return 文件VO
     */
    FileVO upload(MultipartFile file, String businessType, Long businessId);

    /**
     * 上传图片（带压缩和缩略图）
     *
     * @param file         图片文件
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @return 文件VO
     */
    FileVO uploadImage(MultipartFile file, String businessType, Long businessId);

    /**
     * 批量上传文件
     *
     * @param files        文件列表
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @return 文件VO列表
     */
    List<FileVO> uploadBatch(List<MultipartFile> files, String businessType, Long businessId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean delete(Long fileId);

    /**
     * 根据URL删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteByUrl(String fileUrl);

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件VO
     */
    FileVO getFile(Long fileId);

    /**
     * 根据业务获取文件列表
     *
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @return 文件VO列表
     */
    List<FileVO> getFilesByBusiness(String businessType, Long businessId);

    /**
     * 检查文件是否存在（通过MD5，用于秒传）
     *
     * @param md5 文件MD5
     * @return 如果存在返回文件VO，否则返回null
     */
    FileVO checkFileExists(String md5);
}
