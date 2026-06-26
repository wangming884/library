package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Announcement;
import com.library.mapper.AnnouncementMapper;
import com.library.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public PageResult<Announcement> listPage(int page, int size, Integer type) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1);
        if (type != null) wrapper.eq(Announcement::getType, type);
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getPublishTime);

        Page<Announcement> result = announcementMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public boolean publish(Announcement announcement) {
        if (announcement.getId() != null) {
            return announcementMapper.updateById(announcement) > 0;
        }
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public boolean toggleTop(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) throw new RuntimeException("公告不存在");
        announcement.setIsTop(announcement.getIsTop() == 1 ? 0 : 1);
        return announcementMapper.updateById(announcement) > 0;
    }

    @Override
    public void incrementView(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement != null) {
            announcement.setViewCount(announcement.getViewCount() + 1);
            announcementMapper.updateById(announcement);
        }
    }
}
