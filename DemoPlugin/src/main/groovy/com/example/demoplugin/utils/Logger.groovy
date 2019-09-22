package com.example.demoplugin.utils

import org.gradle.api.Project

class Logger {
    static org.gradle.api.logging.Logger logger
    static boolean debug

    static void make(Project project) {
        logger = project.getLogger()
    }

    static void make(Project project, boolean debug) {
        logger = project.getLogger()
        this.debug = debug
    }

    static void i(String info) {
        if (null != info && null != logger && debug) {
            logger.info("DemoPlugin: " + info)
        }
    }

    static void e(String error) {
        if (null != error && null != logger && debug) {
            logger.error("DemoPlugin: " + error)
        }
    }

    static void w(String warning) {
        if (null != warning && null != logger && debug) {
            logger.warn("DemoPlugin: " + warning)
        }
    }
}
