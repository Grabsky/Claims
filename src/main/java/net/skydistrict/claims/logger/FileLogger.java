package net.skydistrict.claims.logger;

import net.skydistrict.claims.Claims;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class FileLogger {
    private static final Claims instance = Claims.getInstance();
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static File file;

    public static void setup() {
        file = new File(instance.getDataFolder() + File.separator + "logs" + File.separator + new SimpleDateFormat("MM-yyyy").format(System.currentTimeMillis()) + ".log");
        if (!file.exists()) {
            createDefaultFile();
        }
    }

    public static void log(String text) {
        if (file.exists()) {
            try {
                PrintWriter wr = new PrintWriter(new FileWriter(file, true));
                wr.write(logDateFormat.format(System.currentTimeMillis()) + " | " + text + System.lineSeparator());
                wr.close();
            } catch (IOException e) {
                instance.getLogger().warning("An error occurred while trying to log a claim action.");
                e.printStackTrace();
            }
        } else if (createDefaultFile()) {
            log(text);
        }
    }

    private static boolean createDefaultFile() {
        try {
            // Creating logs/<date>.log
            file.getParentFile().mkdirs();
            file.createNewFile();
            // Adding header
            PrintWriter wr = new PrintWriter(new FileWriter(file, true));
            wr.write("This file contains logs of plugin actions in following format:" + System.lineSeparator());
            wr.write("  <DATE> | CLAIM_CREATED | <CLAIM_ID> (<CLAIM_LEVEL>) | <CLAIM_CENTER> | <PLAYER>" + System.lineSeparator());
            wr.write("  <DATE> | CLAIM_DESTROYED | <CLAIM_ID> (<CLAIM_LEVEL>) | <CLAIM_CENTER> | <PLAYER> | <OWNER>" + System.lineSeparator());
            wr.write("  <DATE> | CLAIM_UPGRADED | <CLAIM_ID> (<CLAIM_LEVEL>) | <PLAYER> | <OWNER>" + System.lineSeparator());
            wr.write("  <DATE> | MEMBER_ADDED | <CLAIM_ID> | <PLAYER> | <ADDED_PLAYER>" + System.lineSeparator());
            wr.write("  <DATE> | MEMBER_REMOVED | <CLAIM_ID> | <PLAYER> | <REMOVED_PLAYER>" + System.lineSeparator() + System.lineSeparator());
            wr.close();
            return true;
        } catch (IOException e) {
            instance.getLogger().warning("An error occurred while trying to create a log file.");
            return false;
        }
    }
}
