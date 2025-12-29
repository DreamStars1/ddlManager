package com.ddl.manager.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ajax响应结果
 * @author 郑海培
 * @since 2025-12-13
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AjaxResult {

    /** 是否成功 */
    private boolean success;

    /** 错误码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private Object data;

    public static AjaxResult ok() {
        return new AjaxResult(true, 20000, "操作成功", null);
    }

    public static AjaxResult ok(Object data) {
        return new AjaxResult(true, 20000, "操作成功", data);
    }

    public static AjaxResult ok(String message, Object data) {
        return new AjaxResult(true, 20000, message, data);
    }

    public static AjaxResult error(String message) {
        return new AjaxResult(false, 50000, message, null);
    }

    public static AjaxResult error(int code, String message) {
        return new AjaxResult(false, code, message, null);
    }
}
