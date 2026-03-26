package com.study.realtimechat.model.entity;


import com.study.realtimechat.model.enums.FriendInvitationStatus;
import com.study.realtimechat.user.domain.enums.FriendAction;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

import static com.study.realtimechat.model.enums.FriendInvitationStatus.ACCEPTED;
import static com.study.realtimechat.model.enums.FriendInvitationStatus.REJECTED;


@Getter
@Builder
@Table("friend_invitation")
public class FriendInvitationEntity {

    @Id
    private Long id;

    private String fromEmail;

    private String toEmail;

    private FriendInvitationStatus status;

    private Instant createdAt;

    public void accept(FriendAction action) {
        this.status = action == FriendAction.ACCEPTED ? ACCEPTED : REJECTED;
    }
}
