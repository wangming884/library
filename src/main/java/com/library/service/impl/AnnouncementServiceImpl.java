package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Announcement;
import com.library.mapper.AnnouncementMapper;
import com.library.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public PageResult<Announcement> listPage(int page, int size, Integer type) {
        Page<Announcement> result = announcementMapper.selectAnnouncementPage(new Page<>(page, size), type);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public boolean publish(Announcement announcement) {
        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new RuntimeException("请输入公告标题");
        }
        if (!StringUtils.hasText(announcement.getContent())) {
            throw new RuntimeException("请输入公告内容");
        }
        if (announcement.getType() == null) {
            throw new RuntimeException("请选择公告类型");
        }
        if (announcement.getType() < 1 || announcement.getType() > 3) {
            throw new RuntimeException("公告类型不合法");
        }
        announcement.setTitle(announcement.getTitle().trim());
        announcement.setContent(announcement.getContent().trim());
        if (announcement.getId() != null) {
            if (announcementMapper.selectById(announcement.getId()) == null) {
                throw new RuntimeException("公告不存在");
            }
            announcement.setAdminId(null);
            announcement.setPublishTime(null);
            announcement.setViewCount(null);
            if (announcement.getStatus() != null && announcement.getStatus() != 0 && announcement.getStatus() != 1) {
                throw new RuntimeException("公告状态不合法");
            }
            if (announcement.getIsTop() != null && announcement.getIsTop() != 0 && announcement.getIsTop() != 1) {
                throw new RuntimeException("置顶状态不合法");
            }
            return announcementMapper.updateById(announcement) > 0;
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getIsTop() != 0 && announcement.getIsTop() != 1) {
            throw new RuntimeException("置顶状态不合法");
        }
        if (announcement.getViewCount() == null) {
            announcement.setViewCount(0);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getStatus() != 0 && announcement.getStatus() != 1) {
            throw new RuntimeException("公告状态不合法");
        }
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
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
    public boolean removeAnnouncement(Long id) {
        if (announcementMapper.selectById(id) == null) {
            return false;
        }
        return announcementMapper.deleteById(id) > 0;
    }

    @Override
    public void incrementView(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement != null) {
            announcement.setViewCount((announcement.getViewCount() == null ? 0 : announcement.getViewCount()) + 1);
            announcementMapper.updateById(announcement);
        }
    }
}
