package com.nals.rw360.mapper.v1;

import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupCreateReq;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupUpdateReq;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupDetailRes;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupRes;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupSearchRes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubGroupMapper {

    SubGroupMapper INSTANCE = Mappers.getMapper(SubGroupMapper.class);

    SubGroupSearchRes toSubGroupSearchRes(SubGroup subGroup);

    SubGroupRes toSubGroupRes(SubGroup subGroup);

    SubGroup toSubGroup(SubGroupCreateReq subGroupCreateReq);

    SubGroupDetailRes toSubGroupDetail(SubGroup subGroup);

    SubGroup toSubGroup(SubGroupUpdateReq subGroupUpdateReq, @MappingTarget SubGroup subGroup);
}
