package com.example.autobenchmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse 
{
    private ServiceResponseCodeEnum responseCode;
    private String message;
}
