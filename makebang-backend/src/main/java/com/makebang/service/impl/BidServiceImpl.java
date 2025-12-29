package com.makebang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.makebang.common.exception.BusinessException;
import com.makebang.common.result.PageResult;
import com.makebang.common.result.ResultCode;
import com.makebang.dto.bid.CreateBidRequest;
import com.makebang.dto.bid.UpdateBidRequest;
import com.makebang.entity.Bid;
import com.makebang.entity.Project;
import com.makebang.entity.User;
import com.makebang.repository.BidRepository;
import com.makebang.repository.ProjectRepository;
import com.makebang.repository.UserRepository;
import com.makebang.service.BidService;
import com.makebang.service.UserService;
import com.makebang.vo.BidVO;
import com.makebang.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 投标服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BidVO createBid(CreateBidRequest request) {
        UserVO currentUser = userService.getCurrentUser();

        // 验证用户身份（必须是程序员）
        if (currentUser.getUserType() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需求方不能投标");
        }

        // 验证项目是否存在且开放
        Project project = getProjectEntity(request.getProjectId());
        if (project.getStatus() != Project.Status.OPEN.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "该项目不接受投标");
        }

        // 不能对自己的项目投标
        if (project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能对自己的项目投标");
        }

        // 检查是否已投标
        if (bidRepository.countByProjectAndDeveloper(request.getProjectId(), currentUser.getId()) > 0) {
            throw new BusinessException(ResultCode.BID_ALREADY_EXISTS, "您已对该项目投标");
        }

        // 创建投标
        Bid bid = new Bid();
        bid.setProjectId(request.getProjectId());
        bid.setDeveloperId(currentUser.getId());
        bid.setProposedPrice(request.getProposedPrice());
        bid.setProposedDays(request.getProposedDays());
        bid.setProposal(request.getProposal());
        bid.setStatus(Bid.Status.PENDING.code);

        bidRepository.insert(bid);

        // 更新项目投标数
        projectRepository.incrementBidCount(request.getProjectId());

        log.info("用户{}对项目{}投标成功", currentUser.getUsername(), project.getTitle());

        return toVO(bid);
    }

    @Override
    @Transactional
    public BidVO updateBid(Long id, UpdateBidRequest request) {
        Bid bid = getBidEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证所有权
        if (!bid.getDeveloperId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此投标");
        }

        // 只有待处理的投标可以修改
        if (bid.getStatus() != Bid.Status.PENDING.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许修改");
        }

        // 更新字段
        if (request.getProposedPrice() != null) {
            bid.setProposedPrice(request.getProposedPrice());
        }
        if (request.getProposedDays() != null) {
            bid.setProposedDays(request.getProposedDays());
        }
        if (StringUtils.hasText(request.getProposal())) {
            bid.setProposal(request.getProposal());
        }

        bidRepository.updateById(bid);

        return toVO(bid);
    }

    @Override
    @Transactional
    public void withdrawBid(Long id) {
        Bid bid = getBidEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证所有权
        if (!bid.getDeveloperId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权撤回此投标");
        }

        // 只有待处理的投标可以撤回
        if (bid.getStatus() != Bid.Status.PENDING.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许撤回");
        }

        bid.setStatus(Bid.Status.WITHDRAWN.code);
        bidRepository.updateById(bid);

        // 更新项目投标数
        projectRepository.decrementBidCount(bid.getProjectId());

        log.info("用户{}撤回投标{}", currentUser.getUsername(), id);
    }

    @Override
    @Transactional
    public BidVO acceptBid(Long id) {
        Bid bid = getBidEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证项目所有权
        Project project = getProjectEntity(bid.getProjectId());
        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此投标");
        }

        // 验证项目状态
        if (project.getStatus() != Project.Status.OPEN.code) {
            throw new BusinessException(ResultCode.INVALID_PROJECT_STATUS, "项目状态不允许接受投标");
        }

        // 验证投标状态
        if (bid.getStatus() != Bid.Status.PENDING.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投标不是待处理状态");
        }

        // 接受投标
        bid.setStatus(Bid.Status.ACCEPTED.code);
        bidRepository.updateById(bid);

        // 拒绝该项目的其他投标
        rejectOtherBids(bid.getProjectId(), bid.getId());

        // 更新项目状态为进行中
        project.setStatus(Project.Status.IN_PROGRESS.code);
        projectRepository.updateById(project);

        log.info("雇主{}接受了投标{}", currentUser.getUsername(), id);

        // TODO: 创建订单

        return toVO(bid);
    }

    @Override
    @Transactional
    public void rejectBid(Long id) {
        Bid bid = getBidEntity(id);
        UserVO currentUser = userService.getCurrentUser();

        // 验证项目所有权
        Project project = getProjectEntity(bid.getProjectId());
        if (!project.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此投标");
        }

        // 验证投标状态
        if (bid.getStatus() != Bid.Status.PENDING.code) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投标不是待处理状态");
        }

        bid.setStatus(Bid.Status.REJECTED.code);
        bidRepository.updateById(bid);

        log.info("雇主{}拒绝了投标{}", currentUser.getUsername(), id);
    }

    @Override
    public BidVO getBidById(Long id) {
        return toVO(getBidEntity(id));
    }

    @Override
    public List<BidVO> getProjectBids(Long projectId) {
        List<Bid> bids = bidRepository.findByProjectId(projectId);
        return bids.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<BidVO> getMyBids(Integer status, Integer current, Integer size) {
        UserVO currentUser = userService.getCurrentUser();

        LambdaQueryWrapper<Bid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bid::getDeveloperId, currentUser.getId())
                .isNull(Bid::getDeletedAt)
                .orderByDesc(Bid::getCreatedAt);

        if (status != null) {
            wrapper.eq(Bid::getStatus, status);
        }

        Page<Bid> page = new Page<>(current, size);
        IPage<Bid> result = bidRepository.selectPage(page, wrapper);

        List<BidVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public boolean hasBid(Long projectId) {
        UserVO currentUser = userService.getCurrentUser();
        return bidRepository.countByProjectAndDeveloper(projectId, currentUser.getId()) > 0;
    }

    /**
     * 拒绝项目的其他投标
     */
    private void rejectOtherBids(Long projectId, Long acceptedBidId) {
        LambdaQueryWrapper<Bid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bid::getProjectId, projectId)
                .ne(Bid::getId, acceptedBidId)
                .eq(Bid::getStatus, Bid.Status.PENDING.code)
                .isNull(Bid::getDeletedAt);

        Bid updateBid = new Bid();
        updateBid.setStatus(Bid.Status.REJECTED.code);
        bidRepository.update(updateBid, wrapper);
    }

    /**
     * 获取投标实体
     */
    private Bid getBidEntity(Long id) {
        Bid bid = bidRepository.selectById(id);
        if (bid == null || bid.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投标不存在");
        }
        return bid;
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
     * 实体转VO
     */
    private BidVO toVO(Bid bid) {
        if (bid == null) return null;

        BidVO vo = BidVO.builder()
                .id(bid.getId())
                .projectId(bid.getProjectId())
                .developerId(bid.getDeveloperId())
                .proposedPrice(bid.getProposedPrice())
                .proposedDays(bid.getProposedDays())
                .proposal(bid.getProposal())
                .status(bid.getStatus())
                .statusDesc(Bid.Status.getDesc(bid.getStatus()))
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();

        // 加载项目标题
        Project project = projectRepository.selectById(bid.getProjectId());
        if (project != null) {
            vo.setProjectTitle(project.getTitle());
        }

        // 加载投标者信息
        User developer = userRepository.selectById(bid.getDeveloperId());
        if (developer != null) {
            vo.setDeveloper(userService.toVO(developer));
        }

        return vo;
    }
}
