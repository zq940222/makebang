package com.makebang.service;

import com.makebang.common.result.PageResult;
import com.makebang.dto.bid.CreateBidRequest;
import com.makebang.dto.bid.UpdateBidRequest;
import com.makebang.vo.BidVO;

import java.util.List;

/**
 * 投标服务接口
 */
public interface BidService {

    /**
     * 创建投标
     */
    BidVO createBid(CreateBidRequest request);

    /**
     * 更新投标
     */
    BidVO updateBid(Long id, UpdateBidRequest request);

    /**
     * 撤回投标
     */
    void withdrawBid(Long id);

    /**
     * 接受投标（雇主操作）
     */
    BidVO acceptBid(Long id);

    /**
     * 拒绝投标（雇主操作）
     */
    void rejectBid(Long id);

    /**
     * 获取投标详情
     */
    BidVO getBidById(Long id);

    /**
     * 获取项目的投标列表
     */
    List<BidVO> getProjectBids(Long projectId);

    /**
     * 获取当前用户的投标列表
     */
    PageResult<BidVO> getMyBids(Integer status, Integer current, Integer size);

    /**
     * 检查用户是否已对项目投标
     */
    boolean hasBid(Long projectId);
}
