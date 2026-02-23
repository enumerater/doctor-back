package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Constant.*;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.UserMapper;
import com.enumerate.disease_detection.POJO.DTO.*;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.UserLoginVO;
import com.enumerate.disease_detection.Properties.JwtProperties;
import com.enumerate.disease_detection.Utils.CaptchaUtils;
import com.enumerate.disease_detection.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;

    // 原有注册方法（保留，如需移除可删除）
    public String register(UserDTO userDTO) {
        log.info("=== register service ===");
        log.info(userDTO.getCode());
        log.info(redisService.get(userDTO.getEmail()));
        if(redisService.get(userDTO.getEmail()).equals(userDTO.getCode())){
            userMapper.insert(UserPO.builder()
                    .username(userDTO.getUsername())
                    .password(userDTO.getPassword())
                    .role(UserRoleConstant.USER_ROLE_NORMAL_USER)
                    .avatar(AvatarConstant.DEFAULT_AVATAR)
                    .status(UserStatusConstant.USER_NORMAL)
                    .deleted(DeleteConstant.UNDELETED)
                    .email(userDTO.getEmail())
                    .build());
            return "注册成功";
        }
        return "验证码错误";
    }

    // 原有密码登录方法
    public UserLoginVO login(UserDTO userDTO) {
        log.info("=== login service ===");

        String username = userDTO.getUsername();
        String password = userDTO.getPassword();

        // 构造返回对象
        UserLoginVO userLoginVO = UserLoginVO.builder().build();

        UserPO user = userMapper.selectOne(new QueryWrapper<UserPO>().eq("username", username));
        if (user == null) {
            userLoginVO.setMsg(UserLoginConstant.USER_NOT_EXIST);
            return userLoginVO;
        } else {
            if (!user.getPassword().equals(password)) {
                userLoginVO.setMsg(UserLoginConstant.USER_OR_PASSWORD_ERROR);
                return userLoginVO;
            }
            //生成JWT令牌
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());

            String token = JwtUtil.createJWT(jwtProperties.getSecret(), jwtProperties.getExpiration(), claims);
            userLoginVO.setUsername(user.getUsername());
            userLoginVO.setId(String.valueOf(user.getId()));
            userLoginVO.setToken(token);
            userLoginVO.setMsg(UserLoginConstant.USER_SUCCESS);
            userLoginVO.setSessionId(String.valueOf(user.getSessionId()));

            UserContextHolder.setUserId(user.getId());

            userLoginVO.setHasPassword( true);
            userLoginVO.setEmail(user.getEmail());
            userLoginVO.setAvatar(user.getAvatar());

            log.info("登录中");
            log.info("{}",userLoginVO);

            return userLoginVO;
        }
    }

    public UserPO getById(Long id) {
        return userMapper.selectById(id);
    }

    public void updateById(UserSessionUpdateDTO userSessionUpdateDTO) {
        UserPO userPO = userMapper.selectById(userSessionUpdateDTO.getUserId());

        userMapper.updateById(UserPO.builder()
                .id(userSessionUpdateDTO.getUserId())
                .sessionId(userPO.getSessionId() + 1)
                .build());
    }

    // 发送验证码（优化：将验证码存入Redis并设置过期时间）
    public String sendCode(String email) {
        String code = CaptchaUtils.generate6DigitCode();
        // 验证码存入Redis，设置5分钟过期（验证码有效期）
        redisService.setWithExpire(email, code, 300, TimeUnit.SECONDS); // 第三个参数是过期时间，单位秒
        emailService.sendCaptchaEmail(email, code);
        log.info("向邮箱{}发送验证码：{}", email, code);
        return "发送成功";
    }

    // 邮箱验证码登录核心实现
    public UserLoginVO emailLogin(String email, String code) {
        log.info("=== email login service ===");
        UserLoginVO userLoginVO = UserLoginVO.builder().build();

        // 1. 校验验证码
        String redisCode = redisService.get(email);
        if (redisCode == null || !redisCode.equals(code)) {
            userLoginVO.setMsg("验证码错误"); // 需在常量类添加验证码错误常量
            return userLoginVO;
        }

        // 2. 根据邮箱查询用户
        UserPO user = userMapper.selectOne(new QueryWrapper<UserPO>().eq("email", email));

        // 3. 如果用户不存在，自动创建（自动注册）
        if (user == null) {
            log.info("邮箱{}未注册，自动创建账号", email);
            userLoginVO.setHasPassword(false);

            // 生成随机密码（前端不会使用，仅满足数据库非空约束）
            String randomPassword = DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
            // 用户名使用邮箱前缀（也可以用随机字符串）
            String username = email.split("@")[0] + "_" + System.currentTimeMillis();

            UserPO newUser = UserPO.builder()
                    .username(username)
                    .password(randomPassword) // 随机密码，不可逆
                    .email(email)
                    .role(UserRoleConstant.USER_ROLE_NORMAL_USER)
                    .avatar(AvatarConstant.DEFAULT_AVATAR)
                    .status(UserStatusConstant.USER_NORMAL)
                    .deleted(DeleteConstant.UNDELETED)
                    .sessionId(0L) // 初始sessionId
                    .build();
            userMapper.insert(newUser);
            // 获取新创建的用户信息
            user = userMapper.selectOne(new QueryWrapper<UserPO>().eq("email", email));
        }
        else{
            userLoginVO.setHasPassword( true);
        }

        // 4. 生成JWT令牌，完成登录（和密码登录逻辑一致）
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());

        String token = JwtUtil.createJWT(jwtProperties.getSecret(), jwtProperties.getExpiration(), claims);
        userLoginVO.setUsername(user.getUsername());
        userLoginVO.setId(String.valueOf(user.getId()));
        userLoginVO.setToken(token);
        userLoginVO.setMsg(UserLoginConstant.USER_SUCCESS);
        userLoginVO.setSessionId(String.valueOf(user.getSessionId()));

        UserContextHolder.setUserId(user.getId());
        

        userLoginVO.setEmail( email);
        userLoginVO.setAvatar(user.getAvatar());


        log.info("邮箱登录成功，用户信息：{}", userLoginVO);
        // 登录成功后删除Redis中的验证码，防止重复使用
        redisService.delete(email);
        
        

        return userLoginVO;
    }

    public String changePassword(UserPasswordDTO userPasswordDTO) {
        log.info("=== change password service ===");
        UserPO user = userMapper.selectOne(new QueryWrapper<UserPO>().eq("id", UserContextHolder.getUserId()));
        if(Objects.equals(user.getPassword(), userPasswordDTO.getOldPassword())){
            userMapper.updateById(UserPO.builder()
                    .id(user.getId())
                    .password(userPasswordDTO.getNewPassword())
                    .build());
        }
        else{
            return "旧密码错误";
        }
        return "修改成功";
    }

    public String updateProfile(UserNameUpdateDTO userNameUpdateDTO) {
        log.info("=== update profile service ===");
        userMapper.updateById(UserPO.builder()
                .id(UserContextHolder.getUserId())
                .username(userNameUpdateDTO.getUsername())
                .build());

        return "修改成功";

    }

    public String updateAvatar(String avatar) {
        log.info("=== update avatar service ===");
        userMapper.updateById(UserPO.builder()
                .id(UserContextHolder.getUserId())
                .avatar(avatar)
                .build());

        return "修改成功";
    }
}