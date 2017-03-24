package io.cloudslang.lang.compiler.utils;

import io.cloudslang.lang.compiler.SlangSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonczidai Levente
 * @since 3/22/2017
 */
public abstract class SlangSourceUtils {
    public static List<String> readLines(SlangSource source) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getContent()))) {
            String nextLine = getNextLine(reader);
            while (nextLine != null) {
                lines.add(nextLine);
                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + source.getName() + ":" + e.getMessage(), e);
        }
        return lines;
    }

    private static String getNextLine(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
