package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.MockEmployeeApiClient;
import com.reliaquest.api.exception.NotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class EmployeeServiceTest {

    private MockEmployeeApiClient client;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        client = mock(MockEmployeeApiClient.class);
        service = new EmployeeService(client);
    }

    @Test
    void getAll_returnsList() {
        List<Employee> employees = List.of(
                new Employee("1","Alice",100_000,30,"Dev","a@x.com"),
                new Employee("2","Bob",120_000,31,"Dev","b@x.com")
        );
        when(client.fetchAll()).thenReturn(Mono.just(employees));

        assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void searchByName_isCaseInsensitive() {
        List<Employee> employees = List.of(
                new Employee("1","Alice Smith",100_000,30,"Dev","a@x.com"),
                new Employee("2","Bobby",120_000,31,"Dev","b@x.com")
        );
        when(client.fetchAll()).thenReturn(Mono.just(employees));

        assertThat(service.searchByName("alice")).extracting(Employee::employee_name)
                .containsExactly("Alice Smith");
    }

    @Test
    void getById_returnsEmployee() {
        Employee e = new Employee("42","Zoe",170_000,28,"SE","z@x.com");
        when(client.fetchById("42")).thenReturn(Mono.just(e));

        assertThat(service.getById("42").employee_name()).isEqualTo("Zoe");
    }

    @Test
    void getHighestSalary_works() {
        List<Employee> employees = List.of(
                new Employee("1","A",80_000,30,"",""),
                new Employee("2","B",120_000,31,"",""),
                new Employee("3","C",110_000,31,"","")
        );
        when(client.fetchAll()).thenReturn(Mono.just(employees));

        assertThat(service.getHighestSalary()).isEqualTo(120_000);
    }

    @Test
    void top10NamesBySalary_sortedDescLimited() {
        List<Employee> many = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            many.add(new Employee(String.valueOf(i), "Emp"+i, i*1_000, 30, "", ""));
        }
        when(client.fetchAll()).thenReturn(Mono.just(many));

        List<String> top = service.top10NamesBySalary();
        assertThat(top).hasSize(10);
        assertThat(top.get(0)).isEqualTo("Emp12"); // highest
        assertThat(top.get(9)).isEqualTo("Emp3");  // 10th highest
    }

    @Test
    void create_delegates() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("New", 90_000, 25, "JR");
        Employee created = new Employee("99","New", 90_000, 25, "JR", "n@x.com");
        when(client.create(req)).thenReturn(Mono.just(created));

        assertThat(service.create(req).id()).isEqualTo("99");
    }

    @Test
    void deleteById_returnsNameOnSuccess() {
        Employee e = new Employee("5","Del Me", 50_000, 30, "", "");
        when(client.fetchById("5")).thenReturn(Mono.just(e));
        when(client.deleteByName("Del Me")).thenReturn(Mono.just(true));

        assertThat(service.deleteByIdContractBridge("5")).isEqualTo("Del Me");
    }

    @Test
    void deleteById_throwsWhenFalse() {
        Employee e = new Employee("5","Nope", 50_000, 30, "", "");
        when(client.fetchById("5")).thenReturn(Mono.just(e));
        when(client.deleteByName("Nope")).thenReturn(Mono.just(false));

        assertThrows(NotFoundException.class, () -> service.deleteByIdContractBridge("5"));
    }
}
