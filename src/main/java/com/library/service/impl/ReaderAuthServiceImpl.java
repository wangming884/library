package com.library.service.impl;

import com.library.entity.Reader;
import com.library.mapper.ReaderMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.ReaderAuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReaderAuthServiceImpl implements ReaderAuthService {

    private final ReaderMapper readerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public ReaderAuthServiceImpl(ReaderMapper readerMapper, PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider) {
        this.readerMapper = readerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Map<String, Object> readerLogin(String cardNo, String password) {
        Reader reader = readerMapper.findByCardNo(cardNo);
        if (reader == null) {
            throw new RuntimeException("读者证号不存在");
        }
        if (reader.getStatus() != 1) {
            String statusText = switch (reader.getStatus()) {
                case 2 -> "借阅证已挂失";
                case 3 -> "账号已被冻结";
                case 4 -> "已被列入黑名单";
                default -> "账号状态异常";
            };
            throw new RuntimeException(statusText);
        }
        if (!passwordEncoder.matches(password, reader.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成Token (roleKey = "reader")
        String token = jwtTokenProvider.generateToken(reader.getId(), reader.getCardNo(), "reader");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("reader", reader);
        result.put("roleKey", "reader");
        return result;
    }

    @Override
    public void register(Reader reader) {
        // 检查证件号是否已注册
        Reader existing = readerMapper.findByCardNo(reader.getCardNo());
        if (existing != null) {
            throw new RuntimeException("该读者证号已被注册");
        }
        reader.setPassword(passwordEncoder.encode(reader.getPassword()));
        reader.setStatus(1);
        reader.setBalance(java.math.BigDecimal.ZERO);
        readerMapper.insert(reader);
    }
}
