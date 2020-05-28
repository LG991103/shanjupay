package com.shanjupay.common.util;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

import java.io.UnsupportedEncodingException;

public class qiniu {
   public  void test(){

//...生成上传凭证，然后准备上传
       String accessKey = "cOGTaM5WFZiMs34y6e3Vn05A-Trmr9KStfwvfNkT";
       String secretKey = "Udz8K7gbeFRCvJHw4b4Zdw8TaoL9-R8taQJvMa0Y";
       String bucket = "dove-shanju";
       //构造一个带指定 Region 对象的配置类
       Configuration cfg = new Configuration(Region.huadong());
//...其他参数参考类注释
       UploadManager uploadManager = new UploadManager(cfg);
//ng
       String localFilePath = "D:\\home1.png";
//默认不指定key的情况下，以文件内容的hash值作为文件名
       String key = "1O3";
       Auth auth = Auth.create(accessKey, secretKey);
       String upToken = auth.uploadToken(bucket);
       try {
           Response response = uploadManager.put(localFilePath, key, upToken);
           //解析上传成功的结果
           DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
           System.out.println(putRet.key);
           System.out.println(putRet.hash);
       } catch (QiniuException ex) {
           Response r = ex.response;
           System.err.println(r.toString());
           try {
               System.err.println(r.bodyString());
           } catch (QiniuException ex2) {
               //ignore
           }
       }
   }

   public  void  test2(){
       //构造一个带指定 Region 对象的配置类
       Configuration cfg = new Configuration(Region.region0());
//...其他参数参考类注释

       UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
       String accessKey = "cOGTaM5WFZiMs34y6e3Vn05A-Trmr9KStfwvfNkT";
       String secretKey = "Udz8K7gbeFRCvJHw4b4Zdw8TaoL9-R8taQJvMa0Y";
       String bucket = "dove-shanju";
       String key = "456";

//默认不指定key的情况下，以文件内容的hash值作为文件名


       try {
           byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
           Auth auth = Auth.create(accessKey, secretKey);
           String upToken = auth.uploadToken(bucket);

           try {
               Response response = uploadManager.put(uploadBytes, key, upToken);
               //解析上传成功的结果
               DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
               System.out.println(putRet.key);
               System.out.println(putRet.hash);
           } catch (QiniuException ex) {
               Response r = ex.response;
               System.err.println(r.toString());
               try {
                   System.err.println(r.bodyString());
               } catch (QiniuException ex2) {
                   //ignore
               }
           }
       } catch (UnsupportedEncodingException ex) {
           //ignore
       }

   }
public  void  test1(){
    String accessKey = "cOGTaM5WFZiMs34y6e3Vn05A-Trmr9KStfwvfNkT";
    String secretKey = "Udz8K7gbeFRCvJHw4b4Zdw8TaoL9-R8taQJvMa0Y";
    String bucket = "dove-shanju";
    String key = "file key";
    Auth auth = Auth.create(accessKey, secretKey);
    String upToken = auth.uploadToken(bucket, key);
    System.out.println(upToken);
}
    public static void main(String[] args) {
        new qiniu().test();
    }
}
