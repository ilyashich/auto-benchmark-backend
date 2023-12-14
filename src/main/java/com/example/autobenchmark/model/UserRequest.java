package com.example.autobenchmark.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequest 
{
    private String id;

    private String functionName;

    private String sourceCode;

    private String inputs;
}
