package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final EmployeeService service;
    public EmployeeController(EmployeeService service) { this.service = service; }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable("searchString") String searchString) {
        return ResponseEntity.ok(service.searchByName(searchString));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(service.getHighestSalary());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(service.top10NamesBySalary());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody @Valid CreateEmployeeRequest employeeInput) {
        return ResponseEntity.ok(service.create(employeeInput));
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.deleteByIdContractBridge(id));
    }
}
