package com.shanjupay.merchant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class test {
    @Autowired
    RestTemplate template;
    @Test
    public  void  test(){
    String url="http://localhost:56085/sailing/generate?effectiveTime=60&name=sms";
        Map<String,String> body=new HashMap<>();
        body.put("mobile","15228384609");
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity httpEntity=new HttpEntity(body,headers);
        ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        Map body1 = exchange.getBody();

    }
}
