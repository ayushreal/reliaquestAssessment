package com.reliaquest.api.model;


public record Employee(
String id,
String employee_name,
Integer employee_salary,
Integer employee_age,
String employee_title,
String employee_email) {}