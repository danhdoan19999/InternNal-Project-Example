package com.nals.rw360.dto.v1.response.group.sub;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.dto.v1.response.group.sub.leader.SubGroupLeaderRes;
import com.nals.rw360.dto.v1.response.group.type.GroupTypeRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubGroupDetailRes {
    private Long id;
    private String name;
    private String description;
    private String imageName;
    private String imageUrl;
    private GroupTypeRes groupType;
    private SubGroupLeaderRes manager;
}
