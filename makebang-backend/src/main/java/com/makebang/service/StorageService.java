package com.makebang.service;

import java.io.InputStream;

/**
 * 文件存储服务接口
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param filePath    存储路径（含文件名）
     * @param contentType MIME类型
     * @return 文件访问URL
     */
    String upload(InputStream inputStream, String filePath, String contentType);

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否成功
     */
    boolean delete(String filePath);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String filePath);

    /**
     * 获取文件访问URL
     *
     * @param filePath 文件路径
     * @return 访问URL
     */
    String getUrl(String filePath);

    /**
     * 获取存储类型标识
     *
     * @return 存储类型
     */
    String getStorageType();
}
