package com.shanjupay.common.util;

import com.alibaba.fastjson.util.IOUtils;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;

import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
//import com.qiniu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qiniu.storage.Region;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**七牛云测试工具类
 * @author Administrator
 * @version 1.0
 **/
public class QiniuUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);
    /**
     *  文件上传的工具方法
     * @param accessKey
     * @param secretKey
     * @param bucket
     * @param bytes
     * @param fileName 外部传进来，七牛云上的文件名称和此保持一致
     */
    public static void  upload2qiniu(String accessKey,String secretKey,String bucket, byte[] bytes,String fileName) throws RuntimeException{

        Configuration cfg = new Configuration(Region.huadong());

        UploadManager uploadManager = new UploadManager(cfg);


        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(bytes, fileName, upToken);
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

    private static void getdownloadurl() throws UnsupportedEncodingException {
        String fileName = "home.png";
        String domainOfBucket = "http://q9yt4vkpw.bkt.clouddn.com";
        String encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        String accessKey = "cOGTaM5WFZiMs34y6e3Vn05A-Trmr9KStfwvfNkT";
        String secretKey = "Udz8K7gbeFRCvJHw4b4Zdw8TaoL9-R8taQJvMa0Y";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
    }
    public  static void test(){

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
    public static void main(String[] args) throws UnsupportedEncodingException {
        //上传测试
        QiniuUtils.test();
    }
}
