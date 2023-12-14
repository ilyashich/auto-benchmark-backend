package com.example.autobenchmark.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.autobenchmark.dto.ServiceResponse;
import com.example.autobenchmark.dto.ServiceResponseCodeEnum;
import com.example.autobenchmark.model.UserRequest;
import com.example.autobenchmark.service.ICodeService;

import static com.example.autobenchmark.config.Constants.BASE_DIRECTORY;
import static com.example.autobenchmark.config.Constants.TIMEOUT;

@RestController
@RequestMapping("/api")
public class CodeController 
{
    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);

    @Autowired
    private ICodeService codeService;

    @PostMapping("/client/js")
    public ResponseEntity<String> compileClientJS(@RequestBody UserRequest userRequest)
    {
        if(!codeService.createUserDirectory(BASE_DIRECTORY + userRequest.getId()))
        {
            return ResponseEntity.internalServerError().body("Internal server error");
        }

        String workDir = BASE_DIRECTORY + userRequest.getId() + "/";

        String cFilePath = workDir + userRequest.getId() + ".c";

        if(!codeService.saveFile(cFilePath, userRequest.getSourceCode()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user code");
        }

        StringBuilder command = new StringBuilder();
        command.append("emcc ");
        command.append("/app/" + userRequest.getId() + ".c ");
        command.append("-o /app/" + userRequest.getId()  + ".js ");
        command.append("-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free ");
        command.append("-s EXPORTED_RUNTIME_METHODS=ccall ");
        command.append("-s MODULARIZE=1 ");
        command.append("-s EXPORT_ES6=1 ");
        command.append("-s EXPORT_NAME=createModule ");
        command.append("-s WASM=0 ");
        command.append("-s ENVIRONMENT=web ");
        command.append("-O2");

        String emscriptenCommand = "./run_emscripten_container " + 
                                    userRequest.getId() + " " +
                                    "\"" + command.toString() + "\"";

        ServiceResponse response = codeService.compileEmscripten(emscriptenCommand, BASE_DIRECTORY);
        
        if(response.getResponseCode() != ServiceResponseCodeEnum.SUCCESS)
        {
            codeService.cleanup(userRequest.getId());

            String message = "Failed to compile user code";

            if(response.getResponseCode() == ServiceResponseCodeEnum.EXECUTION_ERROR)
            {
                message += ". Reason: " + response.getMessage();
            }         
            return ResponseEntity.internalServerError().body(message);
        }

        return ResponseEntity.ok().body("Successfully compiled user code");
    }

    @PostMapping("/client/wasm")
    public ResponseEntity<String> compileClientWasm(@RequestBody UserRequest userRequest)
    {
        if(!codeService.createUserDirectory(BASE_DIRECTORY + userRequest.getId()))
        {
            return ResponseEntity.internalServerError().body("Internal server error");
        }

        String workDir = BASE_DIRECTORY + userRequest.getId() + "/";

        String cFilePath = workDir + userRequest.getId() + ".c";

        if(!codeService.saveFile(cFilePath, userRequest.getSourceCode()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user code");
        }

        StringBuilder command = new StringBuilder();
        command.append("emcc ");
        command.append("/app/" + userRequest.getId() + ".c ");
        command.append("-o /app/" + userRequest.getId()  + ".js ");
        command.append("-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free ");
        command.append("-s EXPORTED_RUNTIME_METHODS=ccall ");
        command.append("-s MODULARIZE=1 ");
        command.append("-s EXPORT_ES6=1 ");
        command.append("-s EXPORT_NAME=createModule ");
        command.append("-s ENVIRONMENT=web ");
        command.append("-s WASM_BIGINT=1 ");
        command.append("-O2");

        String emscriptenCommand = "./run_emscripten_container " + 
                                    userRequest.getId() + " " +
                                    "\"" +command.toString() + "\"";

        ServiceResponse response = codeService.compileEmscripten(emscriptenCommand, BASE_DIRECTORY);
        
        if(response.getResponseCode() != ServiceResponseCodeEnum.SUCCESS)
        {
            codeService.cleanup(userRequest.getId());

            String message = "Failed to compile user code";

            if(response.getResponseCode() == ServiceResponseCodeEnum.EXECUTION_ERROR)
            {
                message += ". Reason: " + response.getMessage();
            }         
            return ResponseEntity.internalServerError().body(message);
        }

        return ResponseEntity.ok().body("Successfully compiled user code");
    }

    @PostMapping("/server/js")
    public ResponseEntity<String> compileServerJS(@RequestBody UserRequest userRequest)
    {
        if(!codeService.createUserDirectory(BASE_DIRECTORY + userRequest.getId()))
        {
            return ResponseEntity.internalServerError().body("Internal server error");
        }

        String workDir = BASE_DIRECTORY + userRequest.getId() + "/";

        String cFilePath = workDir + userRequest.getId() + ".c";

        if(!codeService.saveFile(cFilePath, userRequest.getSourceCode()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user code");
        }

        String jsonInputsPath = workDir + userRequest.getId() + ".json";

        if(userRequest.getInputs() == null || !codeService.saveFile(jsonInputsPath, userRequest.getInputs()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }

        StringBuilder command = new StringBuilder();
        command.append("emcc ");
        command.append("/app/" + userRequest.getId() + ".c ");
        command.append("-o /app/" + userRequest.getId()  + ".js ");
        command.append("-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free ");
        command.append("-s EXPORTED_RUNTIME_METHODS=ccall ");
        command.append("-s MODULARIZE=1 ");
        command.append("-s EXPORT_NAME=createModule ");
        command.append("-s WASM=0 ");
        command.append("-s ENVIRONMENT=node ");
        command.append("-O2");

        String emscriptenCommand = "./run_emscripten_container " + 
                                    userRequest.getId() + " " +
                                    "\"" +command.toString() + "\"";

        ServiceResponse response = codeService.compileEmscripten(emscriptenCommand, BASE_DIRECTORY);
        
        if(response.getResponseCode() != ServiceResponseCodeEnum.SUCCESS)
        {
            codeService.cleanup(userRequest.getId());

            String message = "Failed to compile user code";

            if(response.getResponseCode() == ServiceResponseCodeEnum.EXECUTION_ERROR)
            {
                message += ". Reason: " + response.getMessage();
            }         
            return ResponseEntity.internalServerError().body(message);
        }

        String runTimeInputStringJSON = "{\n\t\"fileName\": \"" + userRequest.getId() + "\",\n\t\"functionName\": \"" + userRequest.getFunctionName() + "\"\n}";

        String jsonRunTimePath = workDir + "names.json";

        if(userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }

        String benchmarkCommand = "./run_node_container " + 
                                    TIMEOUT + " " + 
                                    userRequest.getId();

        Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

        codeService.cleanup(userRequest.getId());

        if(time != null)
            return ResponseEntity.ok().body(time.toString());
        else
            return ResponseEntity.internalServerError().body("Error while performing benchmark");
    }

    @PostMapping("/server/wasm")
    public ResponseEntity<String> compileServerWasm(@RequestBody UserRequest userRequest)
    {
        if(!codeService.createUserDirectory(BASE_DIRECTORY + userRequest.getId()))
        {
            return ResponseEntity.internalServerError().body("Internal server error");
        }

        String workDir = BASE_DIRECTORY + userRequest.getId() + "/";

        String cFilePath = workDir + userRequest.getId() + ".c";

        if(!codeService.saveFile(cFilePath, userRequest.getSourceCode()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user code");
        }

        String jsonInputsPath = workDir + userRequest.getId() + ".json";

        if(userRequest.getInputs() == null || !codeService.saveFile(jsonInputsPath, userRequest.getInputs()))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }
        
        StringBuilder command = new StringBuilder();
        command.append("emcc ");
        command.append("/app/" + userRequest.getId() + ".c ");
        command.append("-o /app/" + userRequest.getId()  + ".js ");
        command.append("-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free ");
        command.append("-s EXPORTED_RUNTIME_METHODS=ccall ");
        command.append("-s MODULARIZE=1 ");
        command.append("-s EXPORT_NAME=createModule ");
        command.append("-s ENVIRONMENT=node ");
        command.append("-s WASM_BIGINT=1 ");
        command.append("-O2");

        String emscriptenCommand = "./run_emscripten_container " + 
                                    userRequest.getId() + " " +
                                    "\"" +command.toString() + "\"";

        ServiceResponse response = codeService.compileEmscripten(emscriptenCommand, BASE_DIRECTORY);
        
        if(response.getResponseCode() != ServiceResponseCodeEnum.SUCCESS)
        {
            codeService.cleanup(userRequest.getId());

            String message = "Failed to compile user code";

            if(response.getResponseCode() == ServiceResponseCodeEnum.EXECUTION_ERROR)
            {
                message += ". Reason: " + response.getMessage();
            }         
            return ResponseEntity.internalServerError().body(message);
        }

        String runTimeInputStringJSON = "{\n\t\"fileName\": \"" + userRequest.getId() + "\",\n\t\"functionName\": \"" + userRequest.getFunctionName() + "\"\n}";

        String jsonRunTimePath = workDir + "names.json";

        if(userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }

        String benchmarkCommand = "./run_node_container " + 
                                    TIMEOUT + " " + 
                                    userRequest.getId();

        Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

        codeService.cleanup(userRequest.getId());

        if(time != null)
            return ResponseEntity.ok().body(time.toString());
        else
            return ResponseEntity.internalServerError().body("Error while performing benchmark");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFolder(@PathVariable String id)
    {
        codeService.cleanup(id);
        return ResponseEntity.ok().body("Delete request received");
    }
    
}
