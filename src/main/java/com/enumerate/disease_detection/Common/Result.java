package com.enumerate.disease_detection.Common;

import com.enumerate.disease_detection.Constant.ResultCodeConstant;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局统一响应体（权威规范）
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码（非HTTP状态码）
     * 200：成功
     * 4xx：客户端异常（参数、权限等）
     * 5xx：服务端异常
     */
    private int code;

    /**
     * 响应消息（成功/失败描述）
     */
    private String msg;

    /**
     * 响应数据（成功时返回业务数据，失败时可为null）
     */
    private T data;


    // 私有构造，避免外部直接创建
    private Result() {}

    // ========== 静态构造方法（简化使用） ==========
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeConstant.SUCCESS);
        result.setMsg(ResultCodeConstant.SUCCESS_MSG);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 失败响应（使用枚举状态码）
     */
    public static <T> Result<T> error(Integer resultCode) {
        return error(resultCode, ResultCodeConstant.FAIL_MSG);
    }
}