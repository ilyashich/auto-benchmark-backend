package com.example.autobenchmark.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest 
{
    private String id;

    private Integer numberOfExecutions;

    private String functionName;

    private String sourceCode;

    private String inputs;
}
