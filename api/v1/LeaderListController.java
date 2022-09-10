package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.LeaderListBloc;
import com.nals.rw360.dto.v1.request.user.LeaderSearchReq;
import com.nals.rw360.helpers.JsonHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/leaders")
public class LeaderListController
    extends BaseController {

    private final LeaderListBloc leaderListBloc;

    public LeaderListController(final Validator validator, final LeaderListBloc leaderListBloc) {
        super(validator);
        this.leaderListBloc = leaderListBloc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> searchSubGroupLeaders(@RequestParam final Map<String, Object> reqParams) {
        LeaderSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, LeaderSearchReq.class);
        return ok(leaderListBloc.searchSubGroupLeaders(req));
    }
}
