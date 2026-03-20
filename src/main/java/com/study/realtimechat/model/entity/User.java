package com.study.realtimechat.model.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("user")
public class User {

    @Id()
    private Long id;

    @Column("email")
    private String email;
    @Column("password")
    private String password;
    @Column("nickname")
    private String nickname;
    @CreatedDate
    private Instant createdAt;
    @Column("updateAt")
    private Instant updatedAt;
    @Column("deleteAt")
    private Instant deletedAt;
}
