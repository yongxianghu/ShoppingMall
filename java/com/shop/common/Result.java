package com.shop.common;

import lombok.Data;

/**
 * 统一返回格式：{ code, msg, data }。
 * code = 200 表示成功，其它表示失败。
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = ok(data);
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(400, msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
