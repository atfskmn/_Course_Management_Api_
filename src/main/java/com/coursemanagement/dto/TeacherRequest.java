package com.coursemanagement.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class TeacherRequest {
    
    @NotBlank(message = "İsim alanı zorunludur")
    private String name;
    
    @NotBlank(message = "E-posta alanı zorunludur")
    @Email(message = "Geçersiz e-posta formatı")
    private String email;
}