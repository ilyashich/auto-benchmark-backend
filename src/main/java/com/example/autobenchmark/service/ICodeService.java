package com.example.autobenchmark.service;

import com.example.autobenchmark.dto.ServiceResponse;

public interface ICodeService 
{
    public Double compileNode(String command, String folder);
    public ServiceResponse compileEmscripten(String command, String directory);
    public void cleanup(String path);
    public ServiceResponse executeCommand(String command, String directory);
    public boolean createUserDirectory(String path);
    public boolean saveFile(String path, String data);
}
