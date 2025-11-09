package com.coursemanagement.dto;

import com.coursemanagement.entity.Student;
import com.coursemanagement.entity.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer maxStudents;
    private Teacher teacher;
    private List<Student> enrolledStudents;
}
