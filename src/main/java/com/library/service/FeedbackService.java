package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Feedback;

public interface FeedbackService extends IService<Feedback> {

    PageResult<Feedback> listPage(int page, int size, Integer status, Long readerId);

    boolean submit(Feedback feedback);

    boolean reply(Long id, String reply, Long adminId);
}
