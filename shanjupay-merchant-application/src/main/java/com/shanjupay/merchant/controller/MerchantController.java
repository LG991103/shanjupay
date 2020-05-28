package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantDeatilVo2DtoConvert;
import com.shanjupay.merchant.convert.MerchantRegisterVo2DtoConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.service.SmsServiceImpl;
import com.shanjupay.merchant.util.SecurityUtil;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.UUID;

@RestController
@Api(description = "商户平台")
public class MerchantController {
    @Autowired
    FileService fileService;
    @Reference
    MerchantService merchantService;
    @Autowired
    SmsService smsService;

    @GetMapping("/merchants/{id}")
    @ApiOperation(value = "根据商户id查询商户",tags = "查询商户")
    @ApiImplicitParam(name = "id",value = "商户的id",required = true)
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String",
            paramType = "query")
    @GetMapping("/sms")
    public String getSMSCode(String phone){

        String key = smsService.getKey(phone);
        return key;
    }


/*
这个方法不存在 并发操作数据库的问题 有检验验证码管着
 */
    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true, dataType =
            "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegister) {
        // 1.校验
        if (merchantRegister == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
//手机号非空校验
        if (StringUtils.isBlank(merchantRegister.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
//校验手机号的合法性
        if (!PhoneUtil.isMatches(merchantRegister.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
//联系人非空校验
            }
            if (StringUtils.isBlank(merchantRegister.getUsername())) {
                throw new BusinessException(CommonErrorCode.E_100110);
            }
//密码非空校验
            if (StringUtils.isBlank(merchantRegister.getPassword())) {
                throw new BusinessException(CommonErrorCode.E_100111);
            }
//验证码非空校验
            if (StringUtils.isBlank(merchantRegister.getVerifiyCode()) ||
                    StringUtils.isBlank(merchantRegister.getVerifiykey())) {
                throw new BusinessException(CommonErrorCode.E_100103);
            }

//校验验证码
//...
            smsService.checkVerifiyCode(merchantRegister.getVerifiykey(), merchantRegister.getVerifiyCode());
//注册商户
//...
            MerchantDTO merchantDTO = MerchantRegisterVo2DtoConvert.INSTANCE.merchantRegisterVO2MerchantDTO(merchantRegister);

            merchantService.createMerchant(merchantDTO);
            return merchantRegister;


    }
    /*
    名字唯一 不操作数据库 不会出现数据覆盖现象
     */
    @ApiOperation("证件上传")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件", required = true) @RequestParam("file")
                                 MultipartFile file) throws IOException{
        String filename = file.getOriginalFilename();
        String suffix=filename.substring(filename.lastIndexOf(".")-1);
        String name = UUID.randomUUID().toString()+suffix;
        String fileurl = fileService.upload(file.getBytes(), name);
        return fileurl;
    }


    @ApiOperation("资质申请")
    @PostMapping("/my/merchants/save")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")
    })
    public void saveMerchant(@RequestBody MerchantDetailVO merchantInfo){
        //解析token，取出当前登录商户的id
        Long merchantId = SecurityUtil.getMerchantId();

        //Long merchantId,MerchantDTO merchantDTO
        MerchantDTO merchantDTO = MerchantDeatilVo2DtoConvert.INSTANCE.merchantRegisterVO2MerchantDTO(merchantInfo);
        merchantService.applyMerchant(merchantId,merchantDTO);
    }

}
