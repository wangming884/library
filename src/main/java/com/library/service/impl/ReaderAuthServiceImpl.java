package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Reader;
import com.library.mapper.ReaderMapper;
import com.library.mapper.ReaderTypeMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.ReaderAuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReaderAuthServiceImpl implements ReaderAuthService {

    private final ReaderMapper readerMapper;
    private final ReaderTypeMapper readerTypeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public ReaderAuthServiceImpl(ReaderMapper readerMapper, ReaderTypeMapper readerTypeMapper, PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider) {
        this.readerMapper = readerMapper;
        this.readerTypeMapper = readerTypeMapper;
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
        prepareReader(reader);
        if (readerMapper.selectCount(new LambdaQueryWrapper<Reader>().eq(Reader::getCardNo, reader.getCardNo())) > 0) {
            throw new RuntimeException("该读者证号已被注册");
        }
        if (StringUtils.hasText(reader.getPhone())
                && readerMapper.selectCount(new LambdaQueryWrapper<Reader>().eq(Reader::getPhone, reader.getPhone())) > 0) {
            throw new RuntimeException("手机号已被使用");
        }
        if (StringUtils.hasText(reader.getIdCard())
                && readerMapper.selectCount(new LambdaQueryWrapper<Reader>().eq(Reader::getIdCard, reader.getIdCard())) > 0) {
            throw new RuntimeException("证件号已被使用");
        }
        reader.setPassword(passwordEncoder.encode(reader.getPassword()));
        reader.setStatus(1);
        reader.setBalance(BigDecimal.ZERO);
        readerMapper.insert(reader);
    }

    private void prepareReader(Reader reader) {
        if (!StringUtils.hasText(reader.getCardNo())) {
            throw new RuntimeException("请输入读者证号");
        }
        if (!StringUtils.hasText(reader.getName())) {
            throw new RuntimeException("请输入姓名");
        }
        if (!StringUtils.hasText(reader.getPassword())) {
            throw new RuntimeException("请输入密码");
        }
        if (reader.getTypeId() == null) {
            throw new RuntimeException("请选择读者类型");
        }
        if (readerTypeMapper.selectById(reader.getTypeId()) == null) {
            throw new RuntimeException("读者类型不存在");
        }
        reader.setCardNo(reader.getCardNo().trim());
        reader.setName(reader.getName().trim());
        if (StringUtils.hasText(reader.getPhone())) {
            reader.setPhone(reader.getPhone().trim());
        }
        if (StringUtils.hasText(reader.getEmail())) {
            reader.setEmail(reader.getEmail().trim());
        }
        if (StringUtils.hasText(reader.getDept())) {
            reader.setDept(reader.getDept().trim());
        }
        if (StringUtils.hasText(reader.getIdCard())) {
            reader.setIdCard(reader.getIdCard().trim());
        }
    }
}
