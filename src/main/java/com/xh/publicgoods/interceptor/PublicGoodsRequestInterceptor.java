package com.xh.publicgoods.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xh.publicgoods.constants.ResultConstants;
import com.xh.publicgoods.enums.ResultEnum;
import com.xh.publicgoods.util.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.mockito.internal.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class PublicGoodsRequestInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        Map<String, ?> parameterMap = request.getParameterMap();
        try {
            if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
                MultiRequestControl multipleRequestControl = ((HandlerMethod) handler).getMethodAnnotation(MultiRequestControl.class);
                if (multipleRequestControl == null) {
                    log.info("PublicGoodsRequestInterceptor.preHandle() multipleRequestControl is null params::"+ JSON.toJSONString(servletPath)+" , "+JSON.toJSONString(parameterMap) );
                    return true;
                }
                StringBuffer params = new StringBuffer();
                if (!CollectionUtils.isEmpty(parameterMap)) {
                    Iterator<? extends Map.Entry<String, ?>> it = parameterMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ?> next = it.next();
                        String param = next.getKey();
                        Object o = next.getValue();
                        String v="";
                        if (o instanceof Object[]){
                            v = JSONArray.toJSONString(o);
                        }else{
                            v = JSONObject.toJSONString(o);
                        }
                        params.append(param + ":" + v + "|");
                    }
                }

                String second = multipleRequestControl.second();
                if (StringUtils.isEmpty(second)) {
                    log.info("PublicGoodsRequestInterceptor.preHandle() success second is null params::"+ JSON.toJSONString(servletPath)+" , "+JSON.toJSONString(parameterMap) );
                    return true;
                }
                int secondI = Integer.parseInt(second);
                if (0 == secondI) {
                    log.info("PublicGoodsRequestInterceptor.preHandle() success secondI is 0 params::"+ JSON.toJSONString(servletPath)+" , "+JSON.toJSONString(parameterMap) );
                    return true;
                }
                String key=servletPath+"_"+params.toString();
                Long setnx = redisHelper.setnx(key, "1", secondI);
                boolean b = setnx == 1;
                if (!b) {
                    log.info("请求过于频繁,token:" + key + ";method" + servletPath);
                    JSONObject json = new JSONObject();
                    json.put(ResultConstants.RES_CODE, ResultEnum.E0000001.name());
                    json.put(ResultConstants.RES_MSG, multipleRequestControl.message());
                    response.setContentType("text/html; charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(json.toString());
                    writer.flush();
                    writer.close();
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("PublicGoodsRequestInterceptor.preHandle() exception params::"+ JSON.toJSONString(servletPath)+" , "+JSON.toJSONString(parameterMap) ,e);
        }

        return true;
    }
}
