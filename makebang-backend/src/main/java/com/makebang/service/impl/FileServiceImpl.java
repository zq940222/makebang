package com.makebang.service.impl;

import com.makebang.config.StorageConfig;
import com.makebang.entity.FileRecord;
import com.makebang.repository.FileRecordRepository;
import com.makebang.service.FileService;
import com.makebang.service.StorageService;
import com.makebang.util.SecurityUtils;
import com.makebang.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StorageService storageService;
    private final StorageConfig storageConfig;
    private final FileRecordRepository fileRecordRepository;

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Override
    @Transactional
    public FileVO upload(MultipartFile file, String businessType, Long businessId) {
        validateFile(file);

        String mimeType = file.getContentType();
        if (isImage(mimeType)) {
            return uploadImage(file, businessType, businessId);
        }

        return doUpload(file, businessType, businessId, false);
    }

    @Override
    @Transactional
    public FileVO uploadImage(MultipartFile file, String businessType, Long businessId) {
        validateImageFile(file);
        return doUpload(file, businessType, businessId, true);
    }

    @Override
    @Transactional
    public List<FileVO> uploadBatch(List<MultipartFile> files, String businessType, Long businessId) {
        return files.stream()
                .map(file -> upload(file, businessType, businessId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean delete(Long fileId) {
        FileRecord record = fileRecordRepository.selectById(fileId);
        if (record == null || record.getStatus() == 0) {
            return false;
        }

        // 检查权限
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        if (currentUserId != null && !currentUserId.equals(record.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new RuntimeException("无权删除此文件");
        }

        // 删除存储中的文件
        storageService.delete(record.getFilePath());

        // 删除缩略图
        if (StringUtils.hasText(record.getThumbnailUrl())) {
            String thumbnailPath = record.getFilePath().replace(".", "_thumb.");
            storageService.delete(thumbnailPath);
        }

        // 软删除记录
        record.setStatus(0);
        fileRecordRepository.updateById(record);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteByUrl(String fileUrl) {
        Optional<FileRecord> optRecord = fileRecordRepository.findByUrl(fileUrl);
        if (optRecord.isEmpty()) {
            return false;
        }
        return delete(optRecord.get().getId());
    }

    @Override
    public FileVO getFile(Long fileId) {
        FileRecord record = fileRecordRepository.selectById(fileId);
        if (record == null || record.getStatus() == 0) {
            return null;
        }
        return toFileVO(record);
    }

    @Override
    public List<FileVO> getFilesByBusiness(String businessType, Long businessId) {
        return fileRecordRepository.findByBusiness(businessType, businessId).stream()
                .map(this::toFileVO)
                .collect(Collectors.toList());
    }

    @Override
    public FileVO checkFileExists(String md5) {
        Optional<FileRecord> optRecord = fileRecordRepository.findByHash(md5);
        return optRecord.map(this::toFileVO).orElse(null);
    }

    // ========== Private Methods ==========

    private FileVO doUpload(MultipartFile file, String businessType, Long businessId, boolean isImage) {
        try {
            Long userId = SecurityUtils.getCurrentUserIdOrNull();
            String originalName = file.getOriginalFilename();
            String fileType = getFileExtension(originalName);
            String mimeType = file.getContentType();
            long fileSize = file.getSize();

            // 计算MD5
            byte[] fileBytes = file.getBytes();
            String hash = DigestUtils.md5DigestAsHex(fileBytes);

            // 检查是否已存在相同文件（秒传）
            Optional<FileRecord> existing = fileRecordRepository.findByHash(hash);
            if (existing.isPresent()) {
                FileRecord record = existing.get();
                // 创建新记录引用相同文件
                FileRecord newRecord = new FileRecord();
                newRecord.setUserId(userId);
                newRecord.setOriginalName(originalName);
                newRecord.setStoredName(record.getStoredName());
                newRecord.setFilePath(record.getFilePath());
                newRecord.setFileUrl(record.getFileUrl());
                newRecord.setThumbnailUrl(record.getThumbnailUrl());
                newRecord.setFileSize(fileSize);
                newRecord.setFileType(fileType);
                newRecord.setMimeType(mimeType);
                newRecord.setStorageType(record.getStorageType());
                newRecord.setHash(hash);
                newRecord.setWidth(record.getWidth());
                newRecord.setHeight(record.getHeight());
                newRecord.setBusinessType(businessType);
                newRecord.setBusinessId(businessId);
                newRecord.setStatus(1);
                fileRecordRepository.insert(newRecord);

                log.info("File already exists, created reference: {}", originalName);
                return toFileVO(newRecord);
            }

            // 生成存储路径
            String storedName = generateStoredName(fileType);
            String filePath = generateFilePath(businessType, storedName);

            // 上传文件
            String fileUrl = storageService.upload(
                    new ByteArrayInputStream(fileBytes),
                    filePath,
                    mimeType
            );

            // 图片处理
            Integer width = null;
            Integer height = null;
            String thumbnailUrl = null;

            if (isImage && isImage(mimeType)) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();

                    // 生成缩略图
                    thumbnailUrl = generateThumbnail(image, filePath, mimeType);
                }
            }

            // 保存记录
            FileRecord record = new FileRecord();
            record.setUserId(userId);
            record.setOriginalName(originalName);
            record.setStoredName(storedName);
            record.setFilePath(filePath);
            record.setFileUrl(fileUrl);
            record.setThumbnailUrl(thumbnailUrl);
            record.setFileSize(fileSize);
            record.setFileType(fileType);
            record.setMimeType(mimeType);
            record.setStorageType(storageService.getStorageType());
            record.setHash(hash);
            record.setWidth(width);
            record.setHeight(height);
            record.setBusinessType(businessType);
            record.setBusinessId(businessId);
            record.setStatus(1);
            fileRecordRepository.insert(record);

            log.info("File uploaded successfully: {} -> {}", originalName, fileUrl);

            return toFileVO(record);

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private String generateThumbnail(BufferedImage originalImage, String originalPath, String mimeType) {
        try {
            int thumbWidth = storageConfig.getThumbnailWidth();
            int thumbHeight = storageConfig.getThumbnailHeight();

            // 计算缩放比例
            double scale = Math.min(
                    (double) thumbWidth / originalImage.getWidth(),
                    (double) thumbHeight / originalImage.getHeight()
            );

            int newWidth = (int) (originalImage.getWidth() * scale);
            int newHeight = (int) (originalImage.getHeight() * scale);

            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // 上传缩略图
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = mimeType.contains("png") ? "png" : "jpg";
            ImageIO.write(thumbnail, format, baos);

            String thumbnailPath = originalPath.replace(".", "_thumb.");
            return storageService.upload(
                    new ByteArrayInputStream(baos.toByteArray()),
                    thumbnailPath,
                    mimeType
            );

        } catch (Exception e) {
            log.warn("Failed to generate thumbnail: {}", e.getMessage());
            return null;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        long maxSize = storageConfig.getMaxFileSize() * 1024L * 1024L;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + storageConfig.getMaxFileSize() + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType != null && !isAllowedType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }

    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        // 检查图片大小
        long maxSize = storageConfig.getMaxImageSize() * 1024L * 1024L;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过 " + storageConfig.getMaxImageSize() + "MB");
        }

        // 检查是否是图片
        String contentType = file.getContentType();
        if (!isImage(contentType)) {
            throw new IllegalArgumentException("请上传图片文件");
        }
    }

    private boolean isAllowedType(String contentType) {
        for (String allowed : storageConfig.getAllowedTypes()) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isImage(String contentType) {
        return contentType != null && IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateStoredName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") +
                (StringUtils.hasText(extension) ? "." + extension : "");
    }

    private String generateFilePath(String businessType, String storedName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("%s/%s/%s",
                StringUtils.hasText(businessType) ? businessType : "other",
                datePath,
                storedName);
    }

    private FileVO toFileVO(FileRecord record) {
        return FileVO.builder()
                .id(record.getId())
                .originalName(record.getOriginalName())
                .url(record.getFileUrl())
                .thumbnailUrl(record.getThumbnailUrl())
                .size(record.getFileSize())
                .type(record.getFileType())
                .mimeType(record.getMimeType())
                .width(record.getWidth())
                .height(record.getHeight())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
