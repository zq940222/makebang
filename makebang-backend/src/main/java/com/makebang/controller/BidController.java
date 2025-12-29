package com.makebang.controller;

import com.makebang.common.result.PageResult;
import com.makebang.common.result.Result;
import com.makebang.dto.bid.CreateBidRequest;
import com.makebang.dto.bid.UpdateBidRequest;
import com.makebang.service.BidService;
import com.makebang.vo.BidVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投标控制器
 */
@Tag(name = "投标管理", description = "投标的创建、修改、接受、拒绝等接口")
@RestController
@RequestMapping("/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @Operation(summary = "创建投标")
    @PostMapping
    public Result<BidVO> createBid(@Valid @RequestBody CreateBidRequest request) {
        return Result.success(bidService.createBid(request));
    }

    @Operation(summary = "更新投标")
    @PutMapping("/{id}")
    public Result<BidVO> updateBid(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBidRequest request
    ) {
        return Result.success(bidService.updateBid(id, request));
    }

    @Operation(summary = "撤回投标")
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdrawBid(@PathVariable Long id) {
        bidService.withdrawBid(id);
        return Result.success();
    }

    @Operation(summary = "接受投标（雇主操作）")
    @PostMapping("/{id}/accept")
    public Result<BidVO> acceptBid(@PathVariable Long id) {
        return Result.success(bidService.acceptBid(id));
    }

    @Operation(summary = "拒绝投标（雇主操作）")
    @PostMapping("/{id}/reject")
    public Result<Void> rejectBid(@PathVariable Long id) {
        bidService.rejectBid(id);
        return Result.success();
    }

    @Operation(summary = "获取投标详情")
    @GetMapping("/{id}")
    public Result<BidVO> getBid(@PathVariable Long id) {
        return Result.success(bidService.getBidById(id));
    }

    @Operation(summary = "获取项目的投标列表")
    @GetMapping("/project/{projectId}")
    public Result<List<BidVO>> getProjectBids(@PathVariable Long projectId) {
        return Result.success(bidService.getProjectBids(projectId));
    }

    @Operation(summary = "获取我的投标列表")
    @GetMapping("/my")
    public Result<PageResult<BidVO>> getMyBids(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(bidService.getMyBids(status, current, size));
    }

    @Operation(summary = "检查是否已投标")
    @GetMapping("/check/{projectId}")
    public Result<Boolean> hasBid(@PathVariable Long projectId) {
        return Result.success(bidService.hasBid(projectId));
    }
}
