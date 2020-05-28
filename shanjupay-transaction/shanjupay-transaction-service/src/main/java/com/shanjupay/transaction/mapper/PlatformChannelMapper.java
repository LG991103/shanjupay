package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.entity.PlatformChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2019-11-15
 */
@Repository
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {
    @Select("SELECT pc.* FROM platform_pay_channel ppc , " +
            " pay_channel pc, platform_channel pla WHERE  " +
            " pc.CHANNEL_CODE=ppc.`PAY_CHANNEL` " +
            " AND pla.CHANNEL_CODE=#{platformChannelCode} " +
            " AND ppc.PLATFORM_CHANNEL=pla.`CHANNEL_CODE` ;")
    public List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannelCode) ;
}
