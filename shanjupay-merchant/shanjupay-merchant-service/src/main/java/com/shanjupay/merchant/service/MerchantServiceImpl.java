package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService{
    @Autowired
    MerchantMapper merchantMapper;
    @Autowired
    StaffMapper staffMapper;
    @Autowired
    StoreMapper storeMapper;
    @Autowired
    StoreStaffMapper storeStaffMapper;
    @Reference
    TenantService tenantService;
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO=new MerchantDTO();

        BeanUtils.copyProperties(merchant,merchantDTO);
        return  merchantDTO;
    }


    /**
     *  注册商户服务接口，接收账号、密码、手机号，为了可扩展性使用merchantDto接收数据
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    @Override
    @Transactional
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException{
        //校验参数的合法性
        if(merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if(org.apache.commons.lang3.StringUtils.isBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        if(org.apache.commons.lang3.StringUtils.isBlank(merchantDTO.getPassword())){
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //手机号格式校验
        if(!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //校验手机号的唯一性
        //根据手机号查询商户表，如果存在记录则说明手机号已存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if(count>0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        //调用SaaS接口
        //构建调用参数
        /***
         1、手机号

         2、账号

         3、密码

         4、租户类型：shanju-merchant

         5、默认套餐：shanju-merchant

         6、租户名称，同账号名

         */
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");//租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");//套餐，根据套餐进行分配权限
        createTenantRequestDTO.setName(merchantDTO.getUsername());//租户名称，和账号名一样

        //如果租户在SaaS已经存在，SaaS直接 返回此租户的信息，否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //获取租户的id
        if(tenantAndAccount == null || tenantAndAccount.getId() == null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        //租户的id
        Long tenantId = tenantAndAccount.getId();

        //租户id在商户表唯一
        //根据租户id从商户表查询，如果存在记录则不允许添加商户
        Integer count1 = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        if(count1>0){
            throw new BusinessException(CommonErrorCode.E_200017);
        }


        //..写入其它属性
        //使用MapStruct进行对象转换
        Merchant merchant = MerchantConvert.INSTANCE.DTO2merchant(merchantDTO);
        //设置所对应的租户的Id
        merchant.setTenantId(tenantId);
        //审核状态为0-未进行资质申请
        merchant.setAuditStatus("0");
        //调用mapper向数据库写入记录
        merchantMapper.insert(merchant);

        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId());//商户id
        StoreDTO store = createStore(storeDTO);

        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile());//手机号
        staffDTO.setUsername(merchantDTO.getUsername());//账号
        staffDTO.setStoreId(store.getId());//员所属门店id
        staffDTO.setMerchantId(merchant.getId());//商户id
       // staffDTO.setStaffStatus(true);

        StaffDTO staff = createStaff(staffDTO);

        //为门店设置管理员
        bindStaffToStore(store.getId(),staff.getId());

        //将dto中写入新增商户的id
//        merchantDTO.setId(merchant.getId());
        //将entity转成dto
        return MerchantConvert.INSTANCE.merchant2DTO(merchant);
    }



    /**
     * 资质申请接口
     * @param merchantId 商户id
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
       if(merchantId==null||merchantDTO==null){
           throw new BusinessException(CommonErrorCode.E_100108);
       }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if(merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //
        Merchant merchant_update = MerchantConvert.INSTANCE.DTO2merchant(merchantDTO);
        merchant_update.setAuditStatus("1");//已申请待审核
        merchant_update.setTenantId(merchant.getTenantId());//租户id
        merchant_update.setId(merchant.getId());
        merchant_update.setMobile(merchant.getMobile());
                //更新
        merchantMapper.updateById(merchant_update);

    }


    /**
     * 商户下新增门店
     * @param storeDTO
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("商户下新增门店"+ JSON.toJSONString(store));
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //参数合法性校验
        if(staffDTO ==  null || org.apache.commons.lang3.StringUtils.isBlank(staffDTO.getMobile())
                || org.apache.commons.lang3.StringUtils.isBlank(staffDTO.getUsername())
                || staffDTO.getStoreId() == null){
            throw new BusinessException(CommonErrorCode.E_300009);

        }

        //在同一个商户下员工的账号唯一
        Boolean existStaffByUserName = isExistStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if(existStaffByUserName){
            throw new BusinessException(CommonErrorCode.E_100114);
        }

        //在同一个商户下员工的手机号唯一
        Boolean existStaffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if(existStaffByMobile){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);
        staffMapper.insert(staff);

        return StaffConvert.INSTANCE.entity2dto(staff);
        }

    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);
    }


    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile 手机号
     * @return
     */
    private boolean isExistStaffByMobile(String mobile, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }



    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getUsername, userName).eq(Staff::getMerchantId,
                merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }

}
