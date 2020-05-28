package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantConvert {
    /*
    MerchantDTO和Merchant实体类的转换
     */
    MerchantConvert INSTANCE=Mappers.getMapper(MerchantConvert.class);
//  me---->DTO
    MerchantDTO merchant2DTO(Merchant merchant);

    //DTO--->merchat;

    Merchant DTO2merchant(MerchantDTO merchantDTO);

}
