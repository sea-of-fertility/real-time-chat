package com.study.realtimechat.model.entity;


import com.study.realtimechat.model.enums.FriendRequestStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;


@Getter
@Builder
@Table("friend_request")
public class FriendRequestEntity {

    @Id
    private Long id;

    private String fromEmail;

    private String toEmail;

    private FriendRequestStatus status;

    private Instant createdAt;
}
