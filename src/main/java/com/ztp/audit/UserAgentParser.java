package com.ztp.audit;

import ua_parser.Client;
import ua_parser.Parser;

public class UserAgentParser {

    private static final Parser PARSER = new Parser();

    public static String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client c = PARSER.parse(userAgent);
        return c.userAgent.family;
    }

    public static String parseOperatingSystem(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client c = PARSER.parse(userAgent);
        return c.os.family;
    }

    public static String parseDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        Client c = PARSER.parse(userAgent);
        return c.device.family; // e.g. "iPhone", "Other" (usually = desktop), "Samsung SM-..." etc.
    }
}