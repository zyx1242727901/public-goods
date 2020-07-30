package com.xh.publicgoods.config;

import com.xh.publicgoods.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.NoPermissionException;
import java.nio.file.AccessDeniedException;

@Component
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Object handlerException(Exception e) throws Exception{
        log.error("GlobalReturnConfig exceptionHandler >> :",e,false);
        Throwable ex = ExceptionUtils.getRootCause(e);
        if(null == ex) {
            ex = e;
        }
//        if(AccessDeniedException.class.getSimpleName().equals(ex.getClass().getSimpleName())){
//            return ResultEnum.E0000002;
//        } else {

//        }
        //统一返回这个
        return ResultEnum.FAIL;
    }
}
