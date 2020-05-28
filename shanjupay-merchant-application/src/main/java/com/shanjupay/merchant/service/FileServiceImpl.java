package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.BatchUpdateException;
@Service
public class FileServiceImpl implements FileService {
    @Value("${oss.qiniu.url}")
    private String qiniuUrl;
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;
    /*
    文件上传   上面的key配置在统一配置文件nacos中
     */
    @Override
    public String upload(byte[] bytes, String fileName) throws BusinessException {
        try {
            QiniuUtils.upload2qiniu(accessKey,secretKey,bucket,bytes,fileName);
          //  QiniuUtils.test();
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }

        return qiniuUrl+fileName;
    }
}
