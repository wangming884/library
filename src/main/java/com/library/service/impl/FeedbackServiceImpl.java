package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Feedback;
import com.library.mapper.FeedbackMapper;
import com.library.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    public PageResult<Feedback> listPage(int page, int size, Integer status, Long readerId) {
        Page<Feedback> result = feedbackMapper.selectFeedbackPage(new Page<>(page, size), status, readerId);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public boolean submit(Feedback feedback) {
        if (feedback.getType() == null || feedback.getType() < 1 || feedback.getType() > 4) {
            throw new RuntimeException("请选择正确的反馈类型");
        }
        if (!StringUtils.hasText(feedback.getContent())) {
            throw new RuntimeException("请输入反馈内容");
        }
        if (StringUtils.hasText(feedback.getTitle())) {
            feedback.setTitle(feedback.getTitle().trim());
        }
        feedback.setContent(feedback.getContent().trim());
        feedback.setStatus(0);
        return feedbackMapper.insert(feedback) > 0;
    }

    @Override
    public boolean reply(Long id, String reply, Long adminId) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) throw new RuntimeException("反馈不存在");
        if (!StringUtils.hasText(reply)) {
            throw new RuntimeException("请输入回复内容");
        }
        feedback.setReply(reply.trim());
        feedback.setAdminId(adminId);
        feedback.setStatus(1);
        feedback.setReplyTime(LocalDateTime.now());
        return feedbackMapper.updateById(feedback) > 0;
    }
}
