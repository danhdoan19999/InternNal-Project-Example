//package com.nals.rw360.api.v1;
//
//import com.nals.rw360.bloc.v1.CategoryListBloc;
//import com.nals.rw360.dto.v1.request.category.CategorySearchReq;
//import com.nals.rw360.helpers.JsonHelper;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.validation.Validator;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/categories")
//public class CategoryListController
//    extends BaseController {
//
//    private final CategoryListBloc categoryListBloc;
//
//    public CategoryListController(final Validator validator, final CategoryListBloc categoryListBloc) {
//        super(validator);
//        this.categoryListBloc = categoryListBloc;
//    }
//
//    @GetMapping("/list")
//    public ResponseEntity<?> fetchAllCategories() {
//        return ok(categoryListBloc.fetchAllCategories());
//    }
//
//    @GetMapping
//    public ResponseEntity<?> searchCategories(@RequestParam final Map<String, Object> reqParams) {
//        CategorySearchReq req = JsonHelper.MAPPER.convertValue(reqParams, CategorySearchReq.class);
//        return ok(categoryListBloc.searchCategories(req));
//    }
//}
