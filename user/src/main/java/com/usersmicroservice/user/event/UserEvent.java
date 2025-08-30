package com.usersmicroservice.user.event;

import lombok.Data;

import java.util.UUID;

@Data
public class UserEvent {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID companyId;
    private String companyName;
    private EventType type;

    public enum EventType { CREATED, UPDATED, DELETED }
}
