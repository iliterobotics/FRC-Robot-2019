/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.robot.os.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Add your docs here.
 */
public class GetLocalIP {

    /**In windows we want to run in the git bash shell. Update to the correct directory */
    private static final String WINDOWS_SHELL = "C:\\Program Files\\Git\\bin\\bash";
    private static final String WINDOWS_COMMAND = "robot/src/main/resources/sampleOutput.sh";
    private static final String UNIX_SHELL = "/bin/sh";
    private static final String UNIX_SCRIPT ="netstat -t -n | grep tcp | grep -v 127.0.0.1 | awk '{print $4}' | awk -F: '{print $1}'";

    private static final String SHELL;
    private static final String SCRIPT;

    static {
        String os = System.getProperty("os.name");
        String winScript = null;
        if(os.toLowerCase().contains("windows")) {
            String userDir = System.getProperty("user.dir");
            
            //Git-bash uses unix file system representation. 
        String userDirUnix = FilenameUtils.separatorsToUnix(userDir);

            winScript = userDirUnix + "/" + WINDOWS_COMMAND;
            SHELL = WINDOWS_SHELL;
            SCRIPT = winScript;
        } else {
            SHELL = UNIX_SHELL;
            SCRIPT = UNIX_SCRIPT;
        }
    }

    public static Optional<String> getIp() {
        Optional<String>returnVal = Optional.empty();
        ProcessBuilder procBuild = new ProcessBuilder(Arrays.asList(SHELL,"-c",SCRIPT));
        procBuild.redirectErrorStream(true);

        try {
            Process proc = procBuild.start();
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            returnVal = Optional.of(result);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return returnVal;
    }

    public static void main(String[] args) {
        Optional<String> ip = getIp();
        if(ip.isPresent()) {
            System.out.println("IP: " + ip.get());
        } else {
            System.out.println("No IP");
        }
    }
}
