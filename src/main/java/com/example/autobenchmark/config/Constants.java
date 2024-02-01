package com.example.autobenchmark.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constants 
{
    public static String BASE_DIRECTORY;
    public static String TIMEOUT;
    public static String RUN_EMSCRIPTEN_CONTAINER_SCRIPT;
    public static String RUN_NODE_CONTAINER_SCRIPT;

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

    @Value("${run.container.emscripten}")
    public void setRunEmscriptenContainerScript(String value) 
    {
        Constants.RUN_EMSCRIPTEN_CONTAINER_SCRIPT = value;
    }

    @Value("${run.container.node}")
    public void setRunNodeContainerScript(String value) 
    {
        Constants.RUN_NODE_CONTAINER_SCRIPT = value;
    }
}