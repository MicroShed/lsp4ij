/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4ij.server;

import java.util.Locale;

/**
 * Enumerated type for operating systems.
 * <p>
 * Copied from https://github.com/smallrye/smallrye-common/blob/main/os/src/main/java/io/smallrye/common/os/OS.java
 */
public enum OS {

    /**
     * IBM AIX operating system.
     */
    AIX,

    /**
     * Linux-based operating system.
     */
    LINUX,

    /**
     * Apple Macintosh operating system (e.g., macOS).
     */
    MAC,

    /**
     * Oracle Solaris operating system.
     */
    SOLARIS,

    /**
     * Microsoft Windows operating system.
     */
    WINDOWS,

    /**
     * Anything else different from the above.
     */
    OTHER;

    private static final OS CURRENT_OS = determineCurrentOs();

    private static OS determineCurrentOs() {
        return parse(System.getProperty("os.name", "unknown"));
    }

    static OS parse(String osName) {
        osName = osName.toLowerCase(Locale.ENGLISH);

        if (osName.contains("linux")) {
            return LINUX;
        }
        if (osName.contains("windows")) {
            return WINDOWS;
        }
        if (osName.contains("mac") || osName.contains("darwin")) {
            return MAC;
        }
        if (osName.contains("sunos") || osName.contains("solaris")) {
            return SOLARIS;
        }
        if (osName.contains("aix")) {
            return AIX;
        }
        return OTHER;
    }

    /**
     * @return {@code true} if <em>this</em> {@code OS} is known to be the
     * operating system on which the current JVM is executing
     */
    public boolean isCurrent() {
        return this == CURRENT_OS;
    }

    /**
     * @return the current OS
     */
    public static OS current() {
        return CURRENT_OS;
    }
}