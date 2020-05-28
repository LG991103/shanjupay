package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantRegisterVo2DtoConvert {
    MerchantRegisterVo2DtoConvert INSTANCE=Mappers.getMapper(MerchantRegisterVo2DtoConvert.class);
//  VO---->DTO
    MerchantDTO merchantRegisterVO2MerchantDTO(MerchantRegisterVO merchantRegisterVO);
//merchatnDTO2merchantRegisterVO
    //DTO---->VO
    MerchantRegisterVO merchatnDTO2merchantRegisterVO(MerchantDTO merchantDTO);


}
