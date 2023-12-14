package com.example.autobenchmark.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constants 
{
    public static String BASE_DIRECTORY;
    public static String TIMEOUT;

    @Value("${base.directory.path}")
    public void setBaseDirectory(String value)
    {
        Constants.BASE_DIRECTORY = value;
    }

    @Value("${docker.timeout}")
    public void setTimeout(String value) 
    {
        Constants.TIMEOUT = value;
    }
}