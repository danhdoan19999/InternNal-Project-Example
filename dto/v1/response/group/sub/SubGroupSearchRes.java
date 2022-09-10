package com.nals.rw360.dto.v1.response.group.sub;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.dto.v1.response.group.sub.member.SubGroupMemberRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubGroupSearchRes {
    private Long id;
    private String name;
    private String description;
    private String groupType;
    private String imageName;
    private String imageUrl;
    private List<SubGroupMemberRes> users;
}
