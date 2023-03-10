package com.example.demo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import org.springframework.test.web.servlet.MvcResult;
import com.example.demo.student.*;
import java.util.List;
import com.github.javafaker.Faker;
import com.fasterxml.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-it.properties")
public class StudentIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    private Faker faker = new Faker();

    @Test
    void canRegisterNewStudent() throws Exception {
        String name = String.format("%s %s", faker.name().firstName(), faker.name().lastName());
        Student student = new Student(
                name,
                String.format("%s@gmail.com", name.replace(" ", ".").toLowerCase()),
                Gender.FEMALE);
        ResultActions resultActions = mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student)));

        resultActions.andExpect(status().isOk());
        List<Student> students = studentRepository.findAll();
        assertThat(students).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id").contains(student);
    }

    @Test
    void canDeleteStudent() throws Exception {
        String name = String.format("%s %s", faker.name().firstName(), faker.name().lastName());
        String email = String.format("%s@gmail.com", name.replace(" ", ".").toLowerCase());
        Student student = new Student(name, email, Gender.FEMALE);

        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student))).andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/students").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String data = mvcResult.getResponse().getContentAsString();
        List<Student> students = objectMapper.readValue(data, new TypeReference<>() {
        });
        long id = students.stream()
                .filter(s -> s.getEmail().equals(student.getEmail()))
                .map(Student::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "student with email: " + email + " not found"));

        // when
        ResultActions resultActions = mockMvc
                .perform(delete("/api/v1/students/" + id));

        // then
        resultActions.andExpect(status().isOk());
        boolean exists = studentRepository.existsById(id);
        assertThat(exists).isFalse();
    }
}
