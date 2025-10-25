package com.coursemanagement.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CourseRequest {
    
    @NotBlank(message = "Course name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;
    
    @NotNull(message = "Max students is required")
    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents;
    
    // Optional manual availability toggle; if null, keep current value
    private Boolean isAvailable;
    
    // teacherId is determined from path param and authenticated user; no need in request body
}