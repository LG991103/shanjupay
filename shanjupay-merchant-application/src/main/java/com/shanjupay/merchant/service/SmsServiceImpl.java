package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class SmsServiceImpl implements SmsService  {
    @Autowired
    RestTemplate template;
    @Value("${sms.url}")
    String url;
    @Value("${sms.effectiveTime}")
    String effectiveTime;
    @Override
    public String getKey(String phonenum) {

        //向验证码服务发送请求的地址
        String sms_url = url+"/generate?name=sms&effectiveTime="+effectiveTime;

        //请求体
        Map<String,Object> body = new HashMap<>();
        body.put("mobile",phonenum);
        //请求头
        HttpHeaders httpHeaders =new HttpHeaders();
        //指定Content-Type: application/json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //请求信息,传入body，header
        HttpEntity httpEntity = new HttpEntity(body,httpHeaders);
        //向url请求
        ResponseEntity<Map> exchange = null;

        Map bodyMap = null;
        try {
            exchange = template.exchange(sms_url, HttpMethod.POST, httpEntity, Map.class);
           // log.info("请求验证码服务，得到响应:{}", JSON.toJSONString(exchange));
            bodyMap = exchange.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("发送验证码失败");
        }
        if(bodyMap == null || bodyMap.get("result") == null){
            throw new RuntimeException("发送验证码失败");
        }

        Map result = (Map) bodyMap.get("result");
        String key = (String) result.get("key");
     //   log.info("得到发送验证码对应的key:{}",key);
        return key;
    }

    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) throws BusinessException{
        String url_ver = url+"/verify?name=sms&verificationCode="+verifiyCode+"&verificationKey="+verifiyKey;
        Map responseMap = null;
        try {
//请求校验验证码
            ResponseEntity<Map> exchange = template.exchange(url_ver, HttpMethod.POST,
                    HttpEntity.EMPTY, Map.class);
            responseMap = exchange.getBody();
            //log.info("校验验证码，响应内容：{}",JSON.toJSONString(responseMap));
        } catch (Exception e) {
            e.printStackTrace();
            //log.info(e.getMessage(),e);
            //throw new RuntimeException("验证码错误");
            throw  new BusinessException(CommonErrorCode.E_100102);
        }
        if(responseMap == null || responseMap.get("result")==null || !(Boolean) responseMap.get("result")){
         //   throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);
        }

    }
}
