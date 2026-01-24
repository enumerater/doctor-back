package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.Constant.*;
import com.enumerate.disease_detection.Mapper.UserMapper;
import com.enumerate.disease_detection.POJO.DTO.UserDTO;
import com.enumerate.disease_detection.POJO.DTO.UserSessionUpdateDTO;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.UserLoginVO;
import com.enumerate.disease_detection.Properties.JwtProperties;
import com.enumerate.disease_detection.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;


    public String register(UserDTO userDTO) {
        log.info("=== register service ===");
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        try {
            userMapper.insert(UserPO.builder()
                            .username(username)
                            .password(password)
                            .role(UserRoleConstant.USER_ROLE_NORMAL_USER)
                            .avatar(AvatarConstant.DEFAULT_AVATAR)
                            .status(UserStatusConstant.USER_NORMAL)
                            .deleted(DeleteConstant.UNDELETED)
                        .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "注册成功";
    }

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
        }
        else{
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
}
