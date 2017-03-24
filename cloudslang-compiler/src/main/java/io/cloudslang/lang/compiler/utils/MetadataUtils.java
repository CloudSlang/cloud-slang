package io.cloudslang.lang.compiler.utils;

/**
 * @author Bonczidai Levente
 * @since 3/24/2017
 */
public abstract class MetadataUtils {
    public static String generateErrorMessage(int lineNumberZeroBased, String message) {
        return "Error at line [" + (lineNumberZeroBased + 1) + "] - " + message;
    }
}
