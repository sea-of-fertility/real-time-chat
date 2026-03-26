package com.study.realtimechat.friend.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Builder
@Table("friendship")
public class FriendShipEntity {
    @Id
    private Long id;
    private String friendshipA;
    private String friendshipB;
    private Instant createdAt;
}
