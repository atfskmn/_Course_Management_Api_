package com.coursemanagement.repository;

import com.coursemanagement.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByStudentsId(Long studentId);
    List<Course> findByIsAvailableTrue();
    
    @Query("SELECT c FROM Course c WHERE c.isAvailable = true AND c.currentStudentCount < c.maxStudents")
    List<Course> findAvailableCourses();
    
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId")
    List<Course> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.teacher.id = :teacherId")
    Optional<Course> findByIdAndTeacherId(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);
    
    boolean existsByIdAndTeacherId(Long courseId, Long teacherId);
}