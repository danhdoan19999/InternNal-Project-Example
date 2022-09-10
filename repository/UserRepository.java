package com.nals.rw360.repository;

import com.nals.rw360.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
    extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneAndIdIsNot(String phone, Long id);

    Optional<User> findOneById(Long userId);

    Optional<User> findOneByUsername(String username);

    Optional<User> findOneByActivationKey(String activationKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByEmailAndActivatedIsTrue(String username);

    Optional<User> findOneByActivationKeyAndActivatedIsFalse(String activationKey);

    Optional<User> findOneByResetKeyAndActivatedIsTrue(String resetKey);

    User getById(Long id);

    List<User> findUserByIdIn(Collection<Long> ids);

    @Query("SELECT u.email FROM User u WHERE u.activationKey = :key OR u.resetKey = :key")
    String getEmailByKey(@Param("key") String key);

    @Query("SELECT new User(u.id, u.name, u.email, u.phone, u.address, u.gender,"
        + "                 u.dob, u.imageName, u.isFirstLogin)"
        + " FROM User u"
        + " WHERE u.id = :id AND u.activated = TRUE")
    User getBasicInfoById(@Param("id") Long id);

    @Query("SELECT new User(u.id, u.name, usg.subGroupId, u.imageName)"
        + " FROM User u"
        + " INNER JOIN UserSubGroup usg ON u.id = usg.userId"
        + " WHERE usg.subGroupId IN (:subGroupIds) AND u.activated = TRUE")
    List<User> findAllBySubGroupId(@Param("subGroupIds") Iterable<Long> subGroupIds);

    @Query("SELECT u"
        + " FROM User u"
        + " WHERE u.email = :socialEmail"
        + "    OR (u.googleId = :socialId AND 'GOOGLE' = :provider)")
    Optional<User> getBySocialInfo(@Param("socialId") String socialId,
                                   @Param("socialEmail") String socialEmail,
                                   @Param("provider") String provider);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User"
        + " SET remainTryNumber = NULL,"
        + "     lockedDate = NULL,"
        + "     lastLoginDate = :now, "
        + "     lastModifiedBy = :username,"
        + "     lastModifiedDate = :now"
        + " WHERE username = :username")
    void refreshLoginSuccessInfo(@Param("username") String username, @Param("now") Instant now);

    @Query("SELECT u "
        + " FROM User u"
        + " INNER JOIN UserRole ur ON u.id = ur.userId"
        + " INNER JOIN Role r ON r.id = ur.roleId"
        + " WHERE (:keyword IS NULL OR u.name LIKE %:keyword%) AND r.name <> :role")
    List<User> searchUsersByNameIsNotRole(@Param(value = "keyword") String keyword,
                                          @Param(value = "role") String role);

    @Query("SELECT DISTINCT u"
        + " FROM User u"
        + " INNER JOIN UserSubGroup usg ON usg.userId = u.id"
        + " WHERE usg.groupId = :groupId"
        + " AND usg.subGroupId = :subGroupId"
        + " AND (:keyword IS NULL OR u.name LIKE %:keyword%)")
    Page<User> searchByGroupIdAndSubGroupIdAndName(@Param("groupId") Long groupId,
                                                   @Param("subGroupId") Long subGroupId,
                                                   @Param("keyword") String keyword,
                                                   Pageable pageable);

    List<User> findAllByIdNotIn(Collection<Long> ids);

    @Query("SELECT DISTINCT u"
        + " FROM User u"
        + " WHERE u.id IN :ids AND (:keyword IS NULL OR u.name LIKE %:keyword%)")
    Page<User> findByIdAndName(@Param("ids") Collection<Long> ids, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u"
        + " FROM User u"
        + " WHERE :keyword IS NULL OR u.name LIKE %:keyword%")
    Page<User> findByName(@Param("keyword") String keyword, Pageable pageable);
}
