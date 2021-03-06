package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

public interface MerchantService {
    //根据 id查询商户
    MerchantDTO queryMerchantById(Long merchantId);

    /**
     *  注册商户服务接口，接收账号、密码、手机号，为了可扩展性使用merchantDto接收数据
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;


    /**
     * 资质申请接口
     * @param merchantId 商户id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    public void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;



    /**
     * 商户下新增门店
     * @param storeDTO
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;


    /**
     * 商户新增员工
     * @param staffDTO
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;



    /**
     * 为门店设置管理员
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

}
