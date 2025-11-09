package com.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class LoginResponse {
    private String token;
    private Long entityId; // studentId or teacherId
    private String role; // "STUDENT" or "TEACHER"
    private String username;
    private String email;
}
