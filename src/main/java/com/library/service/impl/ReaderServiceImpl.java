package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.Constants;
import com.library.common.PageResult;
import com.library.entity.Reader;
import com.library.entity.ReaderType;
import com.library.mapper.ReaderMapper;
import com.library.mapper.ReaderTypeMapper;
import com.library.service.ReaderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ReaderServiceImpl extends ServiceImpl<ReaderMapper, Reader> implements ReaderService {

    private final ReaderMapper readerMapper;
    private final ReaderTypeMapper readerTypeMapper;

    public ReaderServiceImpl(ReaderMapper readerMapper, ReaderTypeMapper readerTypeMapper) {
        this.readerMapper = readerMapper;
        this.readerTypeMapper = readerTypeMapper;
    }

    @Override
    public PageResult<Reader> listPage(int page, int size, String keyword, Integer status, Long typeId) {
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Reader::getName, keyword)
                    .or().like(Reader::getCardNo, keyword)
                    .or().like(Reader::getPhone, keyword)
                    .or().like(Reader::getIdCard, keyword));
        }
        if (status != null) {
            wrapper.eq(Reader::getStatus, status);
        }
        if (typeId != null) {
            wrapper.eq(Reader::getTypeId, typeId);
        }
        wrapper.orderByDesc(Reader::getCreateTime);

        Page<Reader> result = readerMapper.selectPage(new Page<>(page, size), wrapper);
        // 补充类型名称
        List<ReaderType> types = readerTypeMapper.selectList(null);
        for (Reader r : result.getRecords()) {
            types.stream().filter(t -> t.getId().equals(r.getTypeId())).findFirst()
                    .ifPresent(t -> r.setTypeName(t.getTypeName()));
        }
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Reader getDetail(Long id) {
        return readerMapper.selectWithTypeInfo(id);
    }

    @Override
    public Reader getByCardNo(String cardNo) {
        return readerMapper.findByCardNo(cardNo);
    }

    @Override
    public boolean reportLoss(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setStatus(Constants.READER_STATUS_LOST);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    @Transactional
    public boolean reissue(Long id, String newCardNo) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setCardNo(newCardNo);
        reader.setStatus(Constants.READER_STATUS_NORMAL);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    public boolean freeze(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setStatus(Constants.READER_STATUS_FROZEN);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    public boolean unfreeze(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setStatus(Constants.READER_STATUS_NORMAL);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    public boolean addToBlacklist(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setStatus(Constants.READER_STATUS_BLACKLIST);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    public boolean removeFromBlacklist(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new RuntimeException("读者不存在");
        reader.setStatus(Constants.READER_STATUS_NORMAL);
        return readerMapper.updateById(reader) > 0;
    }

    @Override
    public List<ReaderType> listReaderTypes() {
        return readerTypeMapper.selectList(null);
    }

    @Override
    public boolean saveReaderType(ReaderType readerType) {
        return readerTypeMapper.insert(readerType) > 0;
    }

    @Override
    public boolean updateReaderType(ReaderType readerType) {
        return readerTypeMapper.updateById(readerType) > 0;
    }
}
