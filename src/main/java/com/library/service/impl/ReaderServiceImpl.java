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
    @Transactional
    public boolean updateReader(Reader reader) {
        Reader existing = readerMapper.selectById(reader.getId());
        if (existing == null) {
            throw new RuntimeException("读者不存在");
        }
        prepareReaderForUpdate(reader);
        return readerMapper.updateById(reader) > 0;
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
        if (!StringUtils.hasText(newCardNo)) {
            throw new RuntimeException("请输入新的读者证号");
        }
        newCardNo = newCardNo.trim();
        long duplicateCount = readerMapper.selectCount(new LambdaQueryWrapper<Reader>()
                .eq(Reader::getCardNo, newCardNo)
                .ne(Reader::getId, id));
        if (duplicateCount > 0) {
            throw new RuntimeException("新的读者证号已存在");
        }
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
        prepareReaderType(readerType, false);
        return readerTypeMapper.insert(readerType) > 0;
    }

    @Override
    public boolean updateReaderType(ReaderType readerType) {
        prepareReaderType(readerType, true);
        return readerTypeMapper.updateById(readerType) > 0;
    }

    private void prepareReaderType(ReaderType readerType, boolean update) {
        if (!StringUtils.hasText(readerType.getTypeName())) {
            throw new RuntimeException("请输入读者类型名称");
        }
        if (!StringUtils.hasText(readerType.getTypeKey())) {
            throw new RuntimeException("请输入读者类型标识");
        }
        readerType.setTypeName(readerType.getTypeName().trim());
        readerType.setTypeKey(readerType.getTypeKey().trim());

        LambdaQueryWrapper<ReaderType> wrapper = new LambdaQueryWrapper<ReaderType>()
                .eq(ReaderType::getTypeKey, readerType.getTypeKey());
        if (update && readerType.getId() != null) {
            wrapper.ne(ReaderType::getId, readerType.getId());
        }
        if (readerTypeMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("读者类型标识已存在");
        }
        if (readerType.getMaxBorrow() == null || readerType.getMaxBorrow() <= 0) {
            readerType.setMaxBorrow(5);
        }
        if (readerType.getMaxDays() == null || readerType.getMaxDays() <= 0) {
            readerType.setMaxDays(30);
        }
        if (readerType.getRenewTimes() == null || readerType.getRenewTimes() < 0) {
            readerType.setRenewTimes(0);
        }
        if (readerType.getCanBorrowRare() == null) {
            readerType.setCanBorrowRare(0);
        }
        if (readerType.getStatus() == null) {
            readerType.setStatus(1);
        }
    }

    private void prepareReaderForUpdate(Reader reader) {
        if (reader.getTypeId() != null && readerTypeMapper.selectById(reader.getTypeId()) == null) {
            throw new RuntimeException("读者类型不存在");
        }
        if (StringUtils.hasText(reader.getName())) {
            reader.setName(reader.getName().trim());
        }
        if (StringUtils.hasText(reader.getPhone())) {
            reader.setPhone(reader.getPhone().trim());
            long duplicateCount = readerMapper.selectCount(new LambdaQueryWrapper<Reader>()
                    .eq(Reader::getPhone, reader.getPhone())
                    .ne(Reader::getId, reader.getId()));
            if (duplicateCount > 0) {
                throw new RuntimeException("手机号已被使用");
            }
        }
        if (StringUtils.hasText(reader.getEmail())) {
            reader.setEmail(reader.getEmail().trim());
        }
        if (StringUtils.hasText(reader.getDept())) {
            reader.setDept(reader.getDept().trim());
        }
        if (StringUtils.hasText(reader.getIdCard())) {
            reader.setIdCard(reader.getIdCard().trim());
            long duplicateCount = readerMapper.selectCount(new LambdaQueryWrapper<Reader>()
                    .eq(Reader::getIdCard, reader.getIdCard())
                    .ne(Reader::getId, reader.getId()));
            if (duplicateCount > 0) {
                throw new RuntimeException("证件号已被使用");
            }
        }
        if (reader.getStatus() != null
                && (reader.getStatus() < Constants.READER_STATUS_NORMAL
                || reader.getStatus() > Constants.READER_STATUS_BLACKLIST)) {
            throw new RuntimeException("读者状态不合法");
        }
        reader.setPassword(null);
        reader.setBalance(null);
    }
}
