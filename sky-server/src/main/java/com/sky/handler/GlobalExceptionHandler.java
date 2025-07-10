package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> baseExceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(ex.getMessage());
    }

    /**
     * 重复字段异常处理器
     * @param ex
     * @return {@link Result }
     */
    @ExceptionHandler
    public Result exceptionHandler(DuplicateKeyException ex){
        String message = ex.getMessage();
        log.error("异常信息: {}",message);

        String[] split = message.split(" ");
        String username = split[9];
        String errorMsg = username + MessageConstant.ALREADY_EXISTS;

        return Result.error(errorMsg);
    }

    /**
     * 全局默认异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
