package com.shorturl.util;

import org.springframework.stereotype.Component;

/**
 * 短码生成工具
 * 使用62进制(0-9a-zA-Z)将自增ID转换为短码
 */
@Component
public class ShortCodeUtil {

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = CHARS.length();

    /**
     * 将长ID转换为短码
     * @param id 长ID
     * @return 短码
     */
    public String encode(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID必须大于0");
        }

        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARS.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * 将短码转换为长ID
     * @param code 短码
     * @return 长ID
     */
    public long decode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("短码不能为空");
        }

        long id = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int index = CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("无效的短码字符: " + c);
            }
            id = id * BASE + index;
        }
        return id;
    }

    /**
     * 生成指定长度的短码（补齐前导0）
     * @param id 长ID
     * @param length 目标长度
     * @return 短码
     */
    public String encodeWithPadding(long id, int length) {
        String code = encode(id);
        if (code.length() < length) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length - code.length(); i++) {
                sb.append(CHARS.charAt(0));
            }
            sb.append(code);
            return sb.toString();
        }
        return code;
    }
}