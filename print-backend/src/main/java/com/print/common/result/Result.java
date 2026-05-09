package com.print.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> forbidden(String msg) {
        return error(403, msg);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return error(401, msg);
    }

    public static <T> Result<T> badRequest(String msg) {
        return error(400, msg);
    }
}
