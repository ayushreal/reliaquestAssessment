package com.reliaquest.api.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;

    @MockBean private EmployeeService service;

    @Test
    void getAll_ok() throws Exception {
        List<Employee> list = List.of(
                new Employee("1","Alice",100000,30,"Dev","a@x.com"),
                new Employee("2","Bob",120000,31,"Dev","b@x.com")
        );
        when(service.getAll()).thenReturn(list);

        mvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name").value("Alice"));
    }

    @Test
    void highestSalary_ok() throws Exception {
        when(service.getHighestSalary()).thenReturn(123456);

        mvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("123456"));
    }

    @Test
    void topTen_ok() throws Exception {
        when(service.top10NamesBySalary()).thenReturn(List.of("A","B","C"));

        mvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("B"));
    }

    @Test
    void search_ok() throws Exception {
        when(service.searchByName("al")).thenReturn(List.of(
                new Employee("1","Alice",100000,30,"Dev","a@x.com")
        ));

        mvc.perform(get("/api/v1/employee/search/al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Alice"));
    }

    @Test
    void getById_ok() throws Exception {
        when(service.getById("42")).thenReturn(
                new Employee("42","Zoe",170000,28,"SE","z@x.com")
        );

        mvc.perform(get("/api/v1/employee/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Zoe"));
    }

    @Test
    void create_ok() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest("New", 90000, 25, "JR");
        Employee created = new Employee("99","New",90000,25,"JR","n@x.com");

        when(service.create(req)).thenReturn(created);

        mvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("99"));
    }

    @Test
    void delete_ok() throws Exception {
        when(service.deleteByIdContractBridge("5")).thenReturn("Alice");

        mvc.perform(delete("/api/v1/employee/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Alice"));
    }
}
