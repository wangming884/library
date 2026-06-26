package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Announcement;

public interface AnnouncementService extends IService<Announcement> {

    PageResult<Announcement> listPage(int page, int size, Integer type);

    boolean publish(Announcement announcement);

    boolean toggleTop(Long id);

    void incrementView(Long id);
}
