package com.transyslab.app;

import com.transyslab.simcore.mlp.MLPEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MLP_CMD {
    public static void main(String[] args) {
        {
            MLPEngine mlpEngine = new MLPEngine(args[0]);
            Thread thread = new Thread(){
                @Override
                public void run() {
                    setName(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                    mlpEngine.loadFiles();
                    mlpEngine.repeatRun();
                }
            };
            thread.start();
            Scanner scanner = new Scanner(System.in);
            while (thread.isAlive()) {
                if (scanner.hasNextLine()){
                    switch (scanner.nextLine()) {
                        case "q": {
                            System.out.println("progress is shutting down, please wait......");
                            mlpEngine.stop();
                        }break;
                    }
                }
            }
        }
    }
}
