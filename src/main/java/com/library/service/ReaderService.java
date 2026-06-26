package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Reader;
import com.library.entity.ReaderType;

import java.util.List;

public interface ReaderService extends IService<Reader> {

    /**
     * 分页查询读者
     */
    PageResult<Reader> listPage(int page, int size, String keyword, Integer status, Long typeId);

    /**
     * 获取读者详情（含类型信息）
     */
    Reader getDetail(Long id);

    /**
     * 通过证件号查找
     */
    Reader getByCardNo(String cardNo);

    /**
     * 挂失
     */
    boolean reportLoss(Long id);

    /**
     * 补办
     */
    boolean reissue(Long id, String newCardNo);

    /**
     * 冻结
     */
    boolean freeze(Long id);

    /**
     * 解冻
     */
    boolean unfreeze(Long id);

    /**
     * 加入黑名单
     */
    boolean addToBlacklist(Long id);

    /**
     * 移出黑名单
     */
    boolean removeFromBlacklist(Long id);

    // ---- 读者类型 ----
    List<ReaderType> listReaderTypes();

    boolean saveReaderType(ReaderType readerType);

    boolean updateReaderType(ReaderType readerType);
}
