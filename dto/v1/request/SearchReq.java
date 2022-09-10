package com.nals.rw360.dto.v1.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nals.rw360.helpers.StringHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchReq {
    private String keyword;
    private Integer page;
    private Integer perPage;
    private String sortColumn;
    private String sortType;

    public String getKeyword() {
        return StringHelper.isNotBlank(keyword) ? keyword.trim() : null;
    }
}
