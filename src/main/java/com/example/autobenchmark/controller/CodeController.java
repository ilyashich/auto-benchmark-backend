package com.example.autobenchmark.controller;

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
import static com.example.autobenchmark.config.Constants.RUN_EMSCRIPTEN_CONTAINER_SCRIPT;
import static com.example.autobenchmark.config.Constants.RUN_NODE_CONTAINER_SCRIPT;

@RestController
@RequestMapping("/api")
public class CodeController 
{
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

        String command = "emcc " +
                "/app/" + userRequest.getId() + ".c " +
                "-o /app/" + userRequest.getId() + ".js ";

        if(userRequest.getFunctionName().equals("main"))
        {
            userRequest.setFunctionName("runMain");
            command += "-s ENVIRONMENT=web " +
                       "-s EXPORTED_RUNTIME_METHODS=ccall " +
                       "-s MODULARIZE=1 " +
                       "-s EXPORT_ES6=1 " +
                       "-s EXPORT_NAME=createModule " +
                       "-Dmain=runMain " +
                       "-s EXPORTED_FUNCTIONS=_runMain " +
                       "-s WASM=0 " +
                       "-s ALLOW_MEMORY_GROWTH=1 " +
                       "-O2";
        }
        else
        {
            command += "-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free " +
                       "-s EXPORTED_RUNTIME_METHODS=ccall " +
                       "-s MODULARIZE=1 " +
                       "-s EXPORT_ES6=1 " +
                       "-s EXPORT_NAME=createModule " +
                       "-s WASM=0 " +
                       "-s ENVIRONMENT=web " +
                       "-s ALLOW_MEMORY_GROWTH=1 " +
                       "-O2";
        }

        String emscriptenCommand = RUN_EMSCRIPTEN_CONTAINER_SCRIPT + " " +
                                    userRequest.getId() + " " +
                                    "\"" + command + "\"";

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

        String command = "emcc " +
                "/app/" + userRequest.getId() + ".c " +
                "-o /app/" + userRequest.getId() + ".js ";

        if(userRequest.getFunctionName().equals("main"))
        {
            userRequest.setFunctionName("runMain");
            command += "-s ENVIRONMENT=web " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_ES6=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-Dmain=runMain " +
                    "-s EXPORTED_FUNCTIONS=_runMain " +
                    "-s WASM_BIGINT=1 " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }
        else {

            command += "-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_ES6=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-s ENVIRONMENT=web " +
                    "-s WASM_BIGINT=1 " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }

        String emscriptenCommand = RUN_EMSCRIPTEN_CONTAINER_SCRIPT + " " + 
                                    userRequest.getId() + " " +
                                    "\"" + command + "\"";

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

        if(!userRequest.getFunctionName().equals("main"))
        {
            String jsonInputsPath = workDir + userRequest.getId() + ".json";

            if (userRequest.getInputs() == null || !codeService.saveFile(jsonInputsPath, userRequest.getInputs())) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }
        }

        String command = "emcc " +
                "/app/" + userRequest.getId() + ".c " +
                "-o /app/" + userRequest.getId() + ".js ";

        if(userRequest.getFunctionName().equals("main"))
        {
            userRequest.setFunctionName("runMain");
            command += "-s ENVIRONMENT=node " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-Dmain=runMain " +
                    "-s EXPORTED_FUNCTIONS=_runMain " +
                    "-s WASM=0 " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }
        else {

            command += "-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-s WASM=0 " +
                    "-s ENVIRONMENT=node " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }

        String emscriptenCommand = RUN_EMSCRIPTEN_CONTAINER_SCRIPT + " " + 
                                    userRequest.getId() + " " +
                                    "\"" + command + "\"";

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

        //=========================================================================================================

        if(userRequest.getFunctionName().equals("runMain"))
        {
            String runTimeInputStringJSON = "{\n\t\"fileName\": \"" +
                    userRequest.getId() +
                    "\",\n\t\"repeats\": " +
                    userRequest.getNumberOfExecutions() +
                    "\n}";

            String jsonRunTimePath = workDir + "names.json";

            if (userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON)) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }



            String inputTxtPath = workDir + "input.txt";

            if (userRequest.getInputs() == null || !codeService.saveFile(inputTxtPath, userRequest.getInputs())) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }

            String benchmarkCommand = "./run_node_container_main" + " " +
                    TIMEOUT + " " +
                    userRequest.getId();

            Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

            //codeService.cleanup(userRequest.getId());

            if (time != null)
                return ResponseEntity.ok().body(time.toString());
            else
                return ResponseEntity.internalServerError().body("Error while performing benchmark");
        }

        //===========================================================================================================

        String runTimeInputStringJSON = "{\n\t\"fileName\": \"" +
                userRequest.getId() +
                "\",\n\t\"functionName\": \"" +
                userRequest.getFunctionName() +
                "\",\n\t\"repeats\": " +
                userRequest.getNumberOfExecutions() +
                "\n}";

        String jsonRunTimePath = workDir + "names.json";

        if (userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON)) {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }

        String benchmarkCommand = RUN_NODE_CONTAINER_SCRIPT + " " +
                TIMEOUT + " " +
                userRequest.getId();

        Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

        //codeService.cleanup(userRequest.getId());

        if (time != null)
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

        if(!userRequest.getFunctionName().equals("main"))
        {
            String jsonInputsPath = workDir + userRequest.getId() + ".json";

            if (userRequest.getInputs() == null || !codeService.saveFile(jsonInputsPath, userRequest.getInputs())) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }
        }

        String command = "emcc " +
                "/app/" + userRequest.getId() + ".c " +
                "-o /app/" + userRequest.getId() + ".js ";

        if(userRequest.getFunctionName().equals("main"))
        {
            userRequest.setFunctionName("runMain");
            command += "-s ENVIRONMENT=node " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-Dmain=runMain " +
                    "-s EXPORTED_FUNCTIONS=_runMain " +
                    "-s WASM_BIGINT=1 " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }
        else {

            command += "-s EXPORTED_FUNCTIONS=_" + userRequest.getFunctionName() + ",_malloc,_free " +
                    "-s EXPORTED_RUNTIME_METHODS=ccall " +
                    "-s MODULARIZE=1 " +
                    "-s EXPORT_NAME=createModule " +
                    "-s ENVIRONMENT=node " +
                    "-s WASM_BIGINT=1 " +
                    "-s ALLOW_MEMORY_GROWTH=1 " +
                    "-O2";
        }

        String emscriptenCommand = RUN_EMSCRIPTEN_CONTAINER_SCRIPT + " " +
                                    userRequest.getId() + " " +
                                    "\"" + command + "\"";

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

        //=========================================================================================================

        if(userRequest.getFunctionName().equals("runMain"))
        {
            String runTimeInputStringJSON = "{\n\t\"fileName\": \"" +
                    userRequest.getId() +
                    "\",\n\t\"repeats\": " +
                    userRequest.getNumberOfExecutions() +
                    "\n}";

            String jsonRunTimePath = workDir + "names.json";

            if (userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON)) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }



            String inputTxtPath = workDir + "input.txt";

            if (userRequest.getInputs() == null || !codeService.saveFile(inputTxtPath, userRequest.getInputs())) {
                codeService.cleanup(userRequest.getId());
                return ResponseEntity.internalServerError().body("Failed to save user inputs");
            }

            String benchmarkCommand = "./run_node_container_main" + " " +
                    TIMEOUT + " " +
                    userRequest.getId();

            Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

            //codeService.cleanup(userRequest.getId());

            if (time != null)
                return ResponseEntity.ok().body(time.toString());
            else
                return ResponseEntity.internalServerError().body("Error while performing benchmark");
        }

        //===========================================================================================================



        String runTimeInputStringJSON = "{\n\t\"fileName\": \"" +
                userRequest.getId() +
                "\",\n\t\"functionName\": \"" +
                userRequest.getFunctionName() +
                "\",\n\t\"repeats\": " +
                userRequest.getNumberOfExecutions() +
                "\n}";

        String jsonRunTimePath = workDir + "names.json";

        if(userRequest.getInputs() == null || !codeService.saveFile(jsonRunTimePath, runTimeInputStringJSON))
        {
            codeService.cleanup(userRequest.getId());
            return ResponseEntity.internalServerError().body("Failed to save user inputs");
        }

        String benchmarkCommand = RUN_NODE_CONTAINER_SCRIPT + " " + 
                                    TIMEOUT + " " + 
                                    userRequest.getId();

        Double time = codeService.compileNode(benchmarkCommand, userRequest.getId());

        //codeService.cleanup(userRequest.getId());

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
