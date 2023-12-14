package com.example.autobenchmark.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.autobenchmark.dto.ServiceResponse;
import com.example.autobenchmark.dto.ServiceResponseCodeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

@Service
public class CodeService implements ICodeService
{
    private static final Logger logger = LoggerFactory.getLogger(CodeService.class);

    private final String SAVING_PATH = "/home/illia/emscripten/generated-code/";

    @Override
    public Double compileNode(String command, String folder) 
    {
        ServiceResponse response = executeCommand(command, SAVING_PATH);
        if(response.getResponseCode() == ServiceResponseCodeEnum.SUCCESS)
        {
            logger.info(response.getMessage());
            return readTimeFromJson(folder);
        } 
        else
        {
            logger.error(response.getMessage());
            return null;
        }
    }

    private Double readTimeFromJson(String folder) 
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String filePath = SAVING_PATH + folder + "/output.json";
            JsonNode jsonNode = mapper.readTree(new File(filePath));
            return jsonNode.get("time").asDouble();
        }
        catch(IOException e)
        {
            logger.error("Error while reading time from " + SAVING_PATH + folder + "/output.json. Reason: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ServiceResponse compileEmscripten(String command, String directory) 
    {
        ServiceResponse response = executeCommand(command, directory);
        if(response.getResponseCode() == ServiceResponseCodeEnum.SUCCESS)
        {
            logger.info(response.getMessage());
        }
        else
        {
            logger.error(response.getMessage());
        }
        return response;
    }

    @Override
    public void cleanup(String path)
    {
        try
        {
            FileUtils.deleteDirectory(new File(path));
            logger.info("Successfully deleted directory " + path);
        }
        catch(IOException e)
        {
            logger.error("Error while deleting directory " + path + ". Reason: " + e.getMessage());
        }
    }

    @Override
    public ServiceResponse executeCommand(String command, String directory) 
    {
        ServiceResponse response = new ServiceResponse();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.directory(new File(directory));
            Process process = processBuilder.start();

            try(BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                List<String> lines = infoReader.lines().toList();
                response.setMessage(String.join("\n", lines));
            }

            try(BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
            {
                List<String> errorLines = errorReader.lines().toList();
                //errorLines.forEach(logger::error);
                response.setMessage(String.join("\n", errorLines));
            }

            int exitCode = process.waitFor(); // Waits for the process to complete and returns the exit code

            if (exitCode == 0)
            {
                String message = "Successfully executed \"" + command + "\"";
                if(response.getMessage() != null && !response.getMessage().isEmpty())
                {
                    message += "\n" + response.getMessage();
                }
                response.setMessage(message);
                response.setResponseCode(ServiceResponseCodeEnum.SUCCESS);
            }
            else
            {
                response.setResponseCode(ServiceResponseCodeEnum.EXECUTION_ERROR);
            }
        } catch (IOException | InterruptedException e)
        {
            response.setResponseCode(ServiceResponseCodeEnum.EXCEPTION_WHILE_EXECUTING);
            response.setMessage("Error while executing command \"" + command + "\" : " + e.getMessage());
        }
        return response;
    }

    @Override
    public boolean createUserDirectory(String path)
    {
        try 
        {
            Files.createDirectories(Paths.get(path));
            logger.info("Successfully created directory " + path);
            return true;
        } catch (IOException e) 
        {
            logger.error("Error while creating directory " + path + ". Reason: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveFile(String path, String data)
    {
        try{
            Files.write(Paths.get(path), data.getBytes());
            logger.info("String was successfully saved to file " + path );
            return true;
        } catch (IOException e) {
            logger.error("Error while saving string to the file " + path + ". Reason: " + e.getMessage());
            return false;
        }
    }
}
