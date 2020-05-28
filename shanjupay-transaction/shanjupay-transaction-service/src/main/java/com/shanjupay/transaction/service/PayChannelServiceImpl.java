package com.shanjupay.transaction.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService{
    @Autowired
    private PlatformChannelMapper platformChannelMapper;
    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;
    @Autowired
    private PayChannelParamMapper payChannelParamMapper;
    @Autowired
    Cache redisCache;


    /*
    获取当前平台所支持的所有服务类型

     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        List<PlatformChannelDTO> platformChannelDTOS = PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
        return platformChannelDTOS;
    }





    /**
     * 为app绑定平台服务类型
     * @param appId 应用id
     * @param platformChannelCodes 平台服务类型列表
     *                             根据appid和platformChannelCodes能获得唯一对于的Id
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
         AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes)
        );
         if(appPlatformChannel==null){
             AppPlatformChannel entity=new AppPlatformChannel();
             entity.setAppId(appId);
             entity.setPlatformChannel(platformChannelCodes);


             try {
                 //如果手机和电脑端同时绑定一个服务 appId设为唯一 抛出appId已经存在返回给前端
                 appPlatformChannelMapper.insert(entity);
             }catch (Exception e){
                 throw new BusinessException(CommonErrorCode.E_111112);
             }

         }
    }

    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        int count = appPlatformChannelMapper.selectCount(
                new QueryWrapper<AppPlatformChannel>().lambda().eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
//已存在绑定关系返回1
        if (count > 0) {
            return 1;
        } else {
            return 0;
        }
    }



    /**
     * 根据平台服务类型获取支付渠道列表
     * @param platformChannelCode
     * @return
     * 获取对应支付方式的全部类型 例如  B2C   WXJSAPI
     *                                         支付宝
     *
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }



    /**
     * 保存支付渠道参数
     * @param payChannelParam 商户原始支付渠道参数
     *                        为商户配置 支付宝 微信 支付对应的参数
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException {
        if(payChannelParam == null || StringUtils.isBlank(payChannelParam.getAppId())
                ||
                StringUtils.isBlank(payChannelParam.getPlatformChannelCode())
                ||
                StringUtils.isBlank(payChannelParam.getPayChannel())){
            throw new BusinessException(CommonErrorCode.E_300009);
        }

        //根据appid和服务类型查询应用与服务类型绑定id

        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParam.getAppId(), payChannelParam.getPlatformChannelCode());

        if(appPlatformChannelId == null){
        //应用未绑定该服务类型不可进行支付渠道参数配置
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用与服务类型绑定id和支付渠道查询参数信息
         PayChannelParam channelParam = payChannelParamMapper.selectOne(
                 new LambdaQueryWrapper<PayChannelParam>()
                 .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                 .eq(PayChannelParam::getPayChannel, payChannelParam.getPayChannel())
        );
         if(channelParam!=null){
             channelParam.setChannelName(payChannelParam.getChannelName());
             channelParam.setParam(payChannelParam.getParam());
             payChannelParamMapper.updateById(channelParam);
         }else {

              PayChannelParam newEntiry = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParam);
             newEntiry.setId(null);
             newEntiry.setAppPlatformChannelId(appPlatformChannelId);
             //因为appPlatformChannelId+PayChannel 是唯一的 为其创建了联合唯一索引 防止同时配置同一个应用的同一个付款方式
             try {
                 payChannelParamMapper.insert(newEntiry);
             }catch (Exception e){
                 throw  new BusinessException(CommonErrorCode.E_111113);
             }

         }

         //保存进入reids中
        updateCache(payChannelParam.getAppId(),payChannelParam.getPlatformChannelCode());
    }

    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        //先从redis里面查
         String key = RedisUtil.keyBuilder(appId, platformChannel);
         Boolean exists = redisCache.exists(key);
         if(exists){
             String value = redisCache.get(key);
             List<PayChannelParamDTO>  paramDTOS = JSON.parseArray(value,PayChannelParamDTO.class);
             return paramDTOS;
         }
        //查出应用id和服务类型代码在app_platform_channel主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId,platformChannel);
        if(appPlatformChannelId==null) return  null;
//根据appPlatformChannelId从pay_channel_param查询所有支付参数
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new
                LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId,
                appPlatformChannelId));
        //更新进入redis缓存
        updateCache(appId,platformChannel);
        return PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);

    }

    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId,
                platformChannel);
        for(PayChannelParamDTO payChannelParam:payChannelParamDTOS){
            if(payChannelParam.getPayChannel().equals(payChannel)){
                return payChannelParam;
            }
        }
        return null;

    }

    private void updateCache(String appId, String platformChannelCode) {

        //得到redis中key(付渠道参数配置列表的key)
        //格式：SJ_PAY_PARAM:应用id:服务类型code，例如：SJ_PAY_PARAM：ebcecedd-3032-49a6-9691-4770e66577af：shanju_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
        //根据key查询redis
        Boolean exists = redisCache.exists(redisKey);
        if(exists){
            redisCache.del(redisKey);
        }
        //根据应用id和服务类型code查询支付渠道参数
        //根据应用和服务类型找到它们绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannelCode);
        if(appPlatformChannelId != null){
            //应用和服务类型绑定id查询支付渠道参数记录
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>()
                    .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
            //将payChannelParamDTOS转成json串存入redis
            redisCache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }

    }


    /**
     * 根据appid和服务类型查询应用与服务类型绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
         AppPlatformChannel appPlatformChannelId = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode)
        );
        if(appPlatformChannelId!=null) return  appPlatformChannelId.getId();
        return null;
    }


}
