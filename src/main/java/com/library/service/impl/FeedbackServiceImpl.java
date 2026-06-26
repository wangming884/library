package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Feedback;
import com.library.mapper.FeedbackMapper;
import com.library.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    public PageResult<Feedback> listPage(int page, int size, Integer status, Long readerId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Feedback::getStatus, status);
        if (readerId != null) wrapper.eq(Feedback::getReaderId, readerId);
        wrapper.orderByDesc(Feedback::getCreateTime);

        Page<Feedback> result = feedbackMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public boolean submit(Feedback feedback) {
        feedback.setStatus(0);
        return feedbackMapper.insert(feedback) > 0;
    }

    @Override
    public boolean reply(Long id, String reply, Long adminId) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) throw new RuntimeException("反馈不存在");
        feedback.setReply(reply);
        feedback.setAdminId(adminId);
        feedback.setStatus(1);
        feedback.setReplyTime(LocalDateTime.now());
        return feedbackMapper.updateById(feedback) > 0;
    }
}
