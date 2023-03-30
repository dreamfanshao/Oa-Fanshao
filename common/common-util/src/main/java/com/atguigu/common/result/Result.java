package com.atguigu.common.result;

import lombok.Data;

//获得get、set方法
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result(){

    }

    //封装返回的数据
    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        //封装数据
        if(body !=null){
            result.setData(body);
        }

        //状态码
        result.setCode(resultCodeEnum.getCode());
        //返回信息
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }
    public static<T> Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> ok(T data){
        return build(data,ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }

    public static<T> Result<T> fail(T data){
        return build(data,ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }

}
