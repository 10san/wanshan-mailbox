package com.wanshan.controller.admin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.Result;
import com.wanshan.mapper.AdminMapper;
import com.wanshan.model.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AdminMapper adminMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    private static final int MAX_LOGIN_FAILS = 5;
    private static final int LOCK_MINUTES = 15;

    /**
     * 管理员登录（带失败锁定）
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return Result.error(400, "用户名和密码不能为空");
        }

        // 检查是否被锁定
        String lockKey = "admin_login_lock:" + username;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            return Result.error(429, "尝试次数过多，请" + ttl + "分钟后再试");
        }

        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));

        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            // 记录失败次数
            String failKey = "admin_login_fail:" + username;
            Long fails = redisTemplate.opsForValue().increment(failKey);
            if (fails == 1) {
                redisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
            }
            if (fails != null && fails >= MAX_LOGIN_FAILS) {
                redisTemplate.opsForValue().set(lockKey, "1", LOCK_MINUTES, TimeUnit.MINUTES);
                redisTemplate.delete(failKey);
                return Result.error(429, "尝试次数过多，请" + LOCK_MINUTES + "分钟后再试");
            }
            int remaining = MAX_LOGIN_FAILS - fails.intValue();
            return Result.error(401, "用户名或密码错误，还剩" + remaining + "次机会");
        }

        // 登录成功，清除失败记录
        redisTemplate.delete("admin_login_fail:" + username);
        redisTemplate.delete("admin_login_lock:" + username);

        String token = JWT.create()
                .withSubject(String.valueOf(admin.getId()))
                .withClaim("username", admin.getUsername())
                .withClaim("role", admin.getRole())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpire * 1000))
                .sign(Algorithm.HMAC256(jwtSecret));

        return Result.success(Map.of(
                "token", token,
                "username", admin.getUsername(),
                "role", admin.getRole()
        ));
    }
}
