package com.makebang.controller;

import com.makebang.common.result.Result;
import com.makebang.service.FileService;
import com.makebang.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传单个文件")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId) {
        return Result.success(fileService.upload(file, businessType, businessId));
    }

    @Operation(summary = "上传图片（带缩略图）")
    @PostMapping("/upload/image")
    public Result<FileVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId) {
        return Result.success(fileService.uploadImage(file, businessType, businessId));
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/upload/batch")
    public Result<List<FileVO>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) Long businessId) {
        return Result.success(fileService.uploadBatch(files, businessType, businessId));
    }

    @Operation(summary = "上传头像")
    @PostMapping("/upload/avatar")
    public Result<FileVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadImage(file, "avatar", null));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return Result.success();
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{fileId}")
    public Result<FileVO> getFile(@PathVariable Long fileId) {
        return Result.success(fileService.getFile(fileId));
    }

    @Operation(summary = "根据业务获取文件列表")
    @GetMapping("/business")
    public Result<List<FileVO>> getFilesByBusiness(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        return Result.success(fileService.getFilesByBusiness(businessType, businessId));
    }

    @Operation(summary = "检查文件是否存在（秒传）")
    @GetMapping("/check")
    public Result<FileVO> checkFileExists(@RequestParam String md5) {
        return Result.success(fileService.checkFileExists(md5));
    }
}
