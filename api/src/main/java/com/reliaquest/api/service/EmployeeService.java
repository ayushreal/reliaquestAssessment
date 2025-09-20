package com.reliaquest.api.service;

import com.reliaquest.api.client.MockEmployeeApiClient;
import com.reliaquest.api.exception.NotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Service
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final MockEmployeeApiClient client;

    public EmployeeService(MockEmployeeApiClient client) {
        this.client = client;
    }

    @Cacheable("employees")
    public List<Employee> getAll() {
        log.debug("Fetching all employees from downstream");
        List<Employee> list = client.fetchAll().block();
        return (list != null) ? list : List.of(); // never return null
    }

    public List<Employee> searchByName(String fragment) {
        String needle = fragment == null ? "" : fragment.toLowerCase();
        return getAll().stream()
                .filter(e -> e.employee_name() != null && e.employee_name().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    public Employee getById(String id) {
        try {
            return client.fetchById(id).block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Employee not found: " + id);
            }
            throw e; // propagate 429/5xx so client can retry / caller sees real error
        }
    }

    public int getHighestSalary() {
        return getAll().stream()
                .map(Employee::employee_salary)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> top10NamesBySalary() {
        return getAll().stream()
                .sorted(Comparator.comparing(Employee::employee_salary, Comparator.nullsLast(Integer::compareTo)).reversed())
                .limit(10)
                .map(Employee::employee_name)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee create(CreateEmployeeRequest input) {
        return client.create(input).block();
    }

    @CacheEvict(value = "employees", allEntries = true)
    public String deleteByIdContractBridge(String id) {
    Employee e = getById(id);
    if (e == null) throw new NotFoundException("Employee not found: " + id);
    try {
        Boolean ok = client.deleteByName(e.employee_name()).block();
        if (Boolean.FALSE.equals(ok)) {
            throw new NotFoundException("Delete failed for: " + id);
        }
    } catch (WebClientResponseException w) {
        if (w.getStatusCode().value() == 404) {
            throw new NotFoundException("Employee not found: " + e.employee_name());
        }
        throw w; // let 429/5xx bubble (your retry/backoff already tried)
    }
    return e.employee_name();
    }
}
