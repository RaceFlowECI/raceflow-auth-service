package com.raceflow.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private String email;
    private String name;
    private String sport;
}
