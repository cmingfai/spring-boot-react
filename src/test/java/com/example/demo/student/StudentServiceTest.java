package com.example.demo.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.student.exception.BadRequestException;
import com.example.demo.student.exception.StudentNotFoundException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;
    private StudentService underTest;

    @Captor
    private ArgumentCaptor<Student> studentCaptor;

    @BeforeEach
    void setup() {
        this.underTest = new StudentService(studentRepository);
    }

    @Test
    void canGetAllStudent() {
        // given
        this.underTest.findAllStuedents();

        // then
        verify(studentRepository).findAll();
    }

    @Test
    void canAddStudent() {

        // given
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE);

        // when
        underTest.addStudent(student);

        // then

        // ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        Student capturedStudent = studentCaptor.getValue();
        assertThat(capturedStudent).isEqualTo(student);

    }

    @Test
    void shouldThrowExceptionIfEmailTakenWhenAddStudent() {
        // given
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE);

        // when

        given(studentRepository.selectExistsEmail(student.getEmail())).willReturn(true);

        assertThatThrownBy(() -> underTest.addStudent(student))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email " + student.getEmail() + " is taken.");

        verify(studentRepository, never()).save(student);
    }

    @Test
    void canDeleteStudent() {
        // given
        Long studentId = 1L;

        // when
        given(studentRepository.existsById(studentId)).willReturn(true);

        underTest.removeStudent(studentId);

        verify(studentRepository).deleteById(studentId);
    }

    @Test
    void willThrowExceptionWhenStudentIdNotExists() {
        // given
        Long studentId = 1L;

        assertThatThrownBy(() -> underTest.removeStudent(studentId)).isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Student with id " + studentId + " is not found.");

        verify(studentRepository, never()).deleteById(studentId);
    }
}
