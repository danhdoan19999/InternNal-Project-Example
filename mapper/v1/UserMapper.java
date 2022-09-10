package com.nals.rw360.mapper.v1;

import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.user.ProfileUpdateReq;
import com.nals.rw360.dto.v1.response.group.sub.leader.SubGroupLeaderRes;
import com.nals.rw360.dto.v1.response.group.sub.member.MemberRes;
import com.nals.rw360.dto.v1.response.group.sub.member.SubGroupMemberRes;
import com.nals.rw360.dto.v1.response.user.LeaderRes;
import com.nals.rw360.dto.v1.response.user.ProfileRes;
import com.nals.rw360.helpers.DateHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_NULL;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    SubGroupMemberRes toSubGroupMemberRes(User user);

    ProfileRes toUserBasicInfoRes(User user);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "imageName", nullValuePropertyMappingStrategy = SET_TO_NULL)
    })
    User toUser(ProfileUpdateReq userBasicInfoReq, @MappingTarget User user);

    default Long fromInstant(Instant instant) {
        return DateHelper.toMillis(instant);
    }

    default Instant toInstant(Long millis) {
        return DateHelper.toInstant(millis);
    }

    MemberRes toMemberRes(User user);

    LeaderRes toLeaderRes(User user);

    SubGroupLeaderRes toSubGroupLeaderRes(User user);
}
