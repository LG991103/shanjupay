package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantDeatilVo2DtoConvert {
    MerchantDeatilVo2DtoConvert INSTANCE=Mappers.getMapper(MerchantDeatilVo2DtoConvert.class);
//  VO---->DTO
    MerchantDTO merchantRegisterVO2MerchantDTO(MerchantDetailVO merchantDetailVO);
//merchatnDTO2merchantRegisterVO
    //DTO---->VO
    MerchantDetailVO merchatnDTO2merchantDeatilVO(MerchantDTO merchantDTO);



}
