package us.ilite.robot.os.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Add your docs here.
 */
public class GetLocalIP {

    /**
     * In windows we want to run in the git bash shell. Update to the correct
     * directory
     */
    private static final String kWindowsShell = "C:\\Program Files\\Git\\bin\\bash";
    private static final String kWindowsCommand = "robot/src/main/resources/sampleOutput.sh";
    private static final String kUnixShell = "/bin/sh";
    private static final String UNIX_SCRIPT = "netstat -t -n | grep tcp | grep -v 127.0.0.1 | awk '{print $4}' | awk -F: '{print $1}'";

    private static final String kShell;
    private static final String kScript;

    /**
     * Regex provided by:
     * https://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
     */
    private static final String kIpAddressRegex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final Pattern kIPPattern = Pattern.compile(kIpAddressRegex);

    static {
        String os = System.getProperty("os.name");
        String winScript = null;
        if (os.toLowerCase().contains("windows")) {
            String userDir = System.getProperty("user.dir");

            // Git-bash uses unix file system representation.
            String userDirUnix = FilenameUtils.separatorsToUnix(userDir);

            winScript = userDirUnix + "/" + kWindowsCommand;
            kShell = kWindowsShell;
            kScript = winScript;
        } else {
            kShell = kUnixShell;
            kScript = UNIX_SCRIPT;
        }
    }

    public static Optional<String> getIp() {
        Optional<String> returnVal = Optional.empty();
        ProcessBuilder procBuild = new ProcessBuilder(Arrays.asList(kShell, "-c", kScript));
        procBuild.redirectErrorStream(true);

        try {
            Process proc = procBuild.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            returnVal = getIPFromInputStream(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnVal;
    }

    protected static Optional<String> getIPFromInputStream(BufferedReader reader) {

        Optional<String> returnVal = Optional.empty();
        if (reader != null) {
            Set<String> allLines = new LinkedHashSet<>();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = kIPPattern.matcher(line);
                    if (matcher.matches()) {
                        allLines.add(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnVal = allLines.stream().findFirst();
        }
        return returnVal;
    }

    public static void main(String[] pArgs) {
        Optional<String> ip = getIp();
        if (ip.isPresent()) {
            System.out.println("IP: " + ip.get());
        } else {
            System.out.println("No IP");
        }
    }
}
