package com.wanshan.controller.admin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.Result;
import com.wanshan.mapper.AdminMapper;
import com.wanshan.model.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));

        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

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
