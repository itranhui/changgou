package com.changgou.framework.exception;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.framework.exception
 * 全局异常处理
 ****/
@ControllerAdvice
public class BaseExceptionHandler {

    /***
     * 全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception  e){
        return new Result(false,StatusCode.REMOTEERROR,e.getMessage());
    }
}
