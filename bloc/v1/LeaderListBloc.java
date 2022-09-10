package com.nals.rw360.bloc.v1;

import com.nals.rw360.dto.v1.request.user.LeaderSearchReq;
import com.nals.rw360.dto.v1.response.user.LeaderRes;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderListBloc {
    private final FileService fileService;

    private final UserService userService;

    public List<LeaderRes> searchSubGroupLeaders(final LeaderSearchReq req) {
        log.info("Search sub group leaders by name");

        return userService.searchUsersByNameIsNotRole(req.getKeyword(), ROLE_MEMBER.name())
                          .stream()
                          .map(user -> {
                              LeaderRes leaderRes = UserMapper.INSTANCE.toLeaderRes(user);
                              leaderRes.setImageUrl(fileService.getFullFileUrl(user.getImageName()));
                              return leaderRes;
                          })
                          .collect(Collectors.toList());
    }
}
