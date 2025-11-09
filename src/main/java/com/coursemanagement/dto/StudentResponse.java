package com.coursemanagement.dto;

import com.coursemanagement.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private List<Course> enrolledCourses;
    // Sepet ve sipariş bilgileri de eklenebilir
}
