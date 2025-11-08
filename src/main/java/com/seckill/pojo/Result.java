package com.seckill.pojo;

import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;
    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.message = "success";
        return result;
    }

    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.message = "success";
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.message = msg;
        result.code = 0;
        return result;
    }

    public static Result queue(String msg) {
        Result result = new Result();
        result.message = msg;
        result.code = 2;
        return result;
    }
}
