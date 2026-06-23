package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.entity.Offer;
import com.hrrecruit.module.offer.dto.OfferQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OfferMapper extends BaseMapper<Offer> {

    Page<Offer> selectPageWithDetail(Page<Offer> page,
                                     @Param("query") OfferQueryDTO queryDTO);
}