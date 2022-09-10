package com.nals.rw360.helpers;

import com.nals.rw360.dto.v1.response.DataRes;
import com.nals.rw360.dto.v1.response.PageRes;
import org.springframework.data.domain.Page;

import java.util.List;

public final class ResponseHelper {

    private ResponseHelper() {
    }

    public static DataRes build(final Object data) {
        return new DataRes(data);
    }

    public static PageRes build(final List<?> items) {
        PageRes.Data data = PageRes.Data.builder()
                                        .items(items)
                                        .build();
        return new PageRes(data);
    }

    public static PageRes build(final Page<?> page) {

        List<?> items = page.getContent();

        PageRes.Pagination pagination = PageRes.Pagination.builder()
                                                          .page(page.getNumber() + 1)
                                                          .perPage(page.getSize())
                                                          .total(page.getTotalElements())
                                                          .prev(getPrev(page))
                                                          .next(getNext(page))
                                                          .build();

        PageRes.Data data = PageRes.Data.builder()
                                        .items(items)
                                        .pagination(pagination)
                                        .build();

        return new PageRes(data);
    }

    private static <T> Integer getPrev(final Page<T> page) {
        return page.hasPrevious() ? page.getNumber() : null;
    }

    private static <T> Integer getNext(final Page<T> page) {
        return page.hasNext() ? page.getNumber() + 2 : null;
    }
}
