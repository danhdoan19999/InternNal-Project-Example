package com.nals.rw360.mapper.v1;

import com.nals.rw360.domain.GroupType;
import com.nals.rw360.dto.v1.response.group.type.GroupTypeRes;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupTypeMapper {

    GroupTypeMapper INSTANCE = Mappers.getMapper(GroupTypeMapper.class);

    GroupTypeRes toGroupTypeRes(GroupType groupType);
}
