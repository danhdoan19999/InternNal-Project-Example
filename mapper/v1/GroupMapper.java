package com.nals.rw360.mapper.v1;

import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.GroupCreateReq;
import com.nals.rw360.dto.v1.request.group.GroupUpdateReq;
import com.nals.rw360.dto.v1.response.group.GroupDetailRes;
import com.nals.rw360.dto.v1.response.group.GroupLeaderRes;
import com.nals.rw360.dto.v1.response.group.GroupRes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    Group toGroup(GroupCreateReq groupCreateReq);

    Group toGroup(GroupUpdateReq groupUpdateReq, @MappingTarget Group group);

    GroupRes toGroupRes(Group groups);

    GroupLeaderRes toGroupLeaderRes(User user);

    GroupDetailRes toGroupDetailRes(Group group);
}
