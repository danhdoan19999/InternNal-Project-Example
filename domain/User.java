package com.nals.rw360.domain;

import com.nals.rw360.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import java.time.Instant;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rw_users")
public class User
    extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", length = 60)
    private String password;

    @Column
    private String name;

    @Column(name = "image_name", length = 500)
    private String imageName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Enumerated(STRING)
    @Column(length = 20)
    private Gender gender;

    @Column
    private Instant dob;

    @Size(min = 2, max = 6)
    @Column(name = "lang_key", length = 6, nullable = false)
    private String langKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean activated = false;

    @Column(name = "is_first_login", nullable = false)
    @Builder.Default
    private Boolean isFirstLogin = true;

    @Column(name = "activation_key", length = 500)
    private String activationKey;

    @Column(name = "reset_key", length = 500)
    private String resetKey;

    @Column(name = "remain_try_number")
    private Integer remainTryNumber;

    @Column(name = "locked_date")
    private Instant lockedDate;

    @Column(name = "last_login_date")
    private Instant lastLoginDate;

    @Transient
    private Long subGroupId;

    public User(final Long id, final String name,
                final Long subGroupId, final String imageName) {
        this.id = id;
        this.name = name;
        this.subGroupId = subGroupId;
        this.imageName = imageName;
    }

    public User(final Long id, final String name,
                final String email, final String phone,
                final String address, final Gender gender,
                final Instant dob, final String imageName,
                final boolean isFirstLogin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.dob = dob;
        this.imageName = imageName;
        this.isFirstLogin = isFirstLogin;
    }
}
