package com.klzw.service.python.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class PythonServiceLauncher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PythonServiceLauncher.class);

    private static final String BATCH_FILE_PATH = "C:\\Users\\31783\\Desktop\\MineGuard\\start-python-service.bat";

    private Process pythonProcess;

    @Override
    public void run(String... args) {
        log.info("Starting Python service launcher...");
        startPythonService();
    }

    private void startPythonService() {
        try {
            log.info("Executing batch file: {}", BATCH_FILE_PATH);
            
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", BATCH_FILE_PATH);
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();
            
            log.info("Python service process started, PID: {}", pythonProcess.pid());
            
            readProcessOutput(pythonProcess);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (pythonProcess != null && pythonProcess.isAlive()) {
                    log.info("Shutting down Python service...");
                    pythonProcess.destroy();
                    log.info("Python service stopped");
                }
            }));
            
        } catch (IOException e) {
            log.error("Failed to start Python service", e);
        }
    }

    private void readProcessOutput(Process process) {
        new Thread(() -> {
            try (InputStream is = process.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("[PYTHON OUT] {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading Python process output", e);
            }
        }, "python-output-reader").start();
        
        new Thread(() -> {
            try (InputStream is = process.getErrorStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.error("[PYTHON ERR] {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading Python process error stream", e);
            }
        }, "python-error-reader").start();
    }
}
