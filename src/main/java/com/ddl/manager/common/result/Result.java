package com.ddl.manager.common.result;

import lombok.Data;

/**
 * 统一返回结果类
 * 前后端分离模式下的标准JSON返回格式
 */
@Data
public class Result<T> {
    // 响应码（200成功，500失败，其他自定义）
    private int code;
    // 响应信息
    private String msg;
    // 响应数据
    private T data;

    // 成功响应（带数据）
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 成功响应（无数据）
    public static <T> Result<T> success(String msg) {
        return success(msg, null);
    }

    // 失败响应
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}