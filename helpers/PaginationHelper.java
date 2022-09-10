package com.nals.rw360.helpers;

import com.nals.rw360.dto.v1.request.SearchReq;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PaginationHelper {

    private static final String ASC = "asc";
    private static final String DEFAULT_SORT_PROPERTY = "id";
    private static final int DEFAULT_RECORD_PER_PAGE = 5;
    private static final int DEFAULT_CURRENT_PAGE = 0;

    private PaginationHelper() {
    }

    public static PageRequest generatePageRequest(final SearchReq req) {
        return PageRequest.of(getPage(req.getPage()), getPerPage(req.getPerPage()),
                              generateSort(req.getSortType(), req.getSortColumn()));
    }

    private static Integer getPage(final Integer currentPage) {
        if (Objects.isNull(currentPage)) {
            return DEFAULT_CURRENT_PAGE;
        }
        return currentPage > 0 ? currentPage - 1 : DEFAULT_CURRENT_PAGE;
    }

    private static Integer getPerPage(final Integer limit) {
        if (Objects.isNull(limit)) {
            return DEFAULT_RECORD_PER_PAGE;
        }

        return Math.max(limit, DEFAULT_RECORD_PER_PAGE);
    }

    private static Sort generateSort(final String sortType, final String sortColumn) {
        List<Sort.Order> orders = new ArrayList<>();
        if (Objects.isNull(sortColumn)) {
            orders.add(Sort.Order.desc(DEFAULT_SORT_PROPERTY));
            return Sort.by(orders);
        }
        if (StringHelper.isNotBlank(sortType) && sortType.equalsIgnoreCase(ASC)) {
            orders.add(Sort.Order.asc(sortColumn));
        } else {
            orders.add(Sort.Order.desc(sortColumn));
        }
        return Sort.by(orders);
    }
}
