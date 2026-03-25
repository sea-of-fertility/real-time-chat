package com.study.realtimechat.model.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Builder
@Table("users")
public class User {

    @Id
    @Column("id")
    private Long id;

    @Column("email")
    private String email;
    @Column("password")
    private String password;
    @Column("nickname")
    private String nickname;
    @CreatedDate
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;
    @Column("deleted_at")
    private Instant deletedAt;

    public void encodePassword(String password) {
        this.password = password;
    }
}
