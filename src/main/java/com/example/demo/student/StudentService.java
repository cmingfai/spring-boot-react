package com.example.demo.student;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.demo.student.exception.*;

@Service
@AllArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public List<Student> findAllStuedents() {
        return studentRepository.findAll();
    }

    public void addStudent(Student student) {
        // check if email is taken
        if (studentRepository.selectExistsEmail(student.getEmail())) {
            throw new BadRequestException("Email " + student.getEmail() + " is taken.");
        }
        studentRepository.save(student);
    }

    public void removeStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student with id " + studentId + " is not found.");
        }
        studentRepository.deleteById(studentId);
    }
}
