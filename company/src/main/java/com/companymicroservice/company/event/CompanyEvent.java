package com.companymicroservice.company.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEvent {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID companyId;
    private String companyName;
    private EventType type;

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
