package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.POJO.DTO.*;
import com.enumerate.disease_detection.MVC.POJO.PO.UserPO;
import com.enumerate.disease_detection.MVC.POJO.VO.UserLoginVO;

import com.enumerate.disease_detection.MVC.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @CrossOrigin
    public Result<String> register(@RequestBody UserDTO userDTO) {
        log.info("=== register controller ===");
        log.info(userDTO.toString());
        String res = userService.register(userDTO);
        return Result.success(res);
    }

    /**
     * 用户登录
     */
    @GetMapping("/login")
    @CrossOrigin
    public Result<UserLoginVO> login(HttpServletRequest request, UserDTO userDTO) {
        log.info("=== login controller ===");
        log.info(userDTO.toString());
        String userIp = request.getRemoteAddr();
        log.info(userIp);

        UserLoginVO res = userService.login(userDTO);

        if (Objects.equals(res.getMsg(), "用户不存在")) {
            return Result.error(400, "用户不存在");
        }
        if (Objects.equals(res.getMsg(), "用户名或密码错误")) {
            return Result.error(400, "用户名或密码错误");
        }
        return Result.success(res);
    }

    /**
     * 获取用户信息
     */

    @GetMapping("/getById")
    @CrossOrigin
    public Result<UserPO> getById(@RequestParam Long id) {
        log.info("=== getById controller ===");
        log.info(id.toString());
        UserPO res = userService.getById(id);
        return Result.success(res);
    }

    @PutMapping
    @CrossOrigin
    public Result<String> update(@RequestBody UserSessionUpdateDTO userSessionUpdateDTO) {
        log.info("=== update controller ===");
        log.info(String.valueOf(userSessionUpdateDTO));
        userService.updateById(userSessionUpdateDTO);
        return Result.success("更新成功");
    }

    @PostMapping("/sendCode")
    @CrossOrigin
    public Result<String> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        log.info("=== sendCode controller ===");
        log.info(sendCodeDTO.getEmail());

        String res = userService.sendCode(sendCodeDTO.getEmail());
        return Result.success(res);
    }

    @PostMapping("/emailLogin")
    @CrossOrigin
    public Result<UserLoginVO> emailLogin(@RequestBody UserDTO userDTO) {
        log.info("=== emailLogin controller ===");
        log.info(userDTO.toString());

        UserLoginVO res = userService.emailLogin(userDTO.getEmail(), userDTO.getCode());
        if (Objects.equals(res.getMsg(), "验证码错误")) {
            return Result.error(400, "验证码错误");
        }
        return Result.success(res);
    }

    @PutMapping("/password")
    @CrossOrigin
    public Result<String> changePassword(@RequestBody UserPasswordDTO userPasswordDTO) {
        log.info("=== changePassword controller ===");
        log.info(userPasswordDTO.toString());

        String res = userService.changePassword(userPasswordDTO);
        if (Objects.equals(res, "旧密码错误")) {
            return Result.error(400, "旧密码错误");
        }
        return Result.success(res);
    }

    @PutMapping("/profile")
    @CrossOrigin
    public Result<String> updateProfile(@RequestBody UserNameUpdateDTO userNameUpdateDTO) {
        log.info("=== updateProfile controller ===");
        log.info(userNameUpdateDTO.toString());
        if (userNameUpdateDTO.getUsername() == null) {
            String res = userService.updateAvatar(userNameUpdateDTO.getAvatar());

            return Result.success(res);

        }
        else{
            String res = userService.updateProfile(userNameUpdateDTO);
            return Result.success(res);
        }

    }


}
