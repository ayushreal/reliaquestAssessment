package com.reliaquest.api.model;


public record ApiEnvelope<T>(T data, String status) {}