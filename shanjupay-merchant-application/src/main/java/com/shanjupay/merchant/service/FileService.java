package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

import java.sql.BatchUpdateException;

public interface FileService {
    /*
    上传文件
     */
    public String upload(byte[] bytes,String fileName) throws BusinessException;
}
