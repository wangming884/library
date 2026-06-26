package com.library.service;

import com.library.entity.Reader;
import java.util.Map;

public interface ReaderAuthService {

    /**
     * 读者登录（通过证件号+密码）
     */
    Map<String, Object> readerLogin(String cardNo, String password);

    /**
     * 读者注册
     */
    void register(Reader reader);
}
