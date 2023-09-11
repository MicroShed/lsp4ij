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

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProcessStreamConnectionProvider implements StreamConnectionProvider {
    private @Nullable
    Process process;
    private List<String> commands;
    private @Nullable
    String workingDir;

    public ProcessStreamConnectionProvider() {
    }

    public ProcessStreamConnectionProvider(List<String> commands) {
        this.commands = commands;
    }

    public ProcessStreamConnectionProvider(List<String> commands, String workingDir) {
        this.commands = commands;
        this.workingDir = workingDir;
    }

    @Override
    public void start() throws CannotStartProcessException {
        if (this.commands == null || this.commands.isEmpty() || this.commands.stream().anyMatch(Objects::isNull)) {
            throw new CannotStartProcessException("Unable to start language server: " + this.toString()); //$NON-NLS-1$
        }
        ProcessBuilder builder = createProcessBuilder();
        try {
            Process p = builder.start();
            this.process = p;
        } catch (IOException e) {
            throw new CannotStartProcessException(e);
        }
    }

    @Override
    public boolean isAlive() {
        return process != null ? process.isAlive() : false;
    }

    @Override
    public void ensureIsAlive() throws CannotStartProcessException {
        // Wait few ms before checking the is alive flag.
        synchronized (this.process) {
            try {
                this.process.wait(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!isAlive()) {
            throw new CannotStartProcessException("Unable to start language server: " + this.toString()); //$NON-NLS-1$
        }
    }

    protected ProcessBuilder createProcessBuilder() {
        ProcessBuilder builder = new ProcessBuilder(getCommands());
        if (getWorkingDirectory() != null) {
            builder.directory(new File(getWorkingDirectory()));
        }
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder;
    }

    @Override
    public @Nullable
    InputStream getInputStream() {
        Process p = process;
        return p == null ? null : p.getInputStream();
    }

    @Override
    public @Nullable
    InputStream getErrorStream() {
        Process p = process;
        return p == null ? null : p.getErrorStream();
    }

    @Override
    public @Nullable
    OutputStream getOutputStream() {
        Process p = process;
        return p == null ? null : p.getOutputStream();
    }

    public @Nullable
    Long getPid() {
        final Process p = process;
        return p == null ? null : p.pid();
    }

    @Override
    public void stop() {
        Process p = process;
        if (p != null) {
            p.destroy();
            process = null;
        }
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    protected @Nullable
    String getWorkingDirectory() {
        return workingDir;
    }

    public void setWorkingDirectory(String workingDir) {
        this.workingDir = workingDir;
    }

    protected boolean checkJavaVersion(String javaHome, int expectedVersion) {
        final ProcessBuilder builder = new ProcessBuilder(javaHome +
                File.separator + "bin" + File.separator + "java", "-version");
        try {
            final Process p = builder.start();
            final Reader r = new InputStreamReader(p.getErrorStream());
            final StringBuilder sb = new StringBuilder();
            int i;
            while ((i = r.read()) != -1) {
                sb.append((char) i);
            }
            return parseMajorJavaVersion(sb.toString()) >= expectedVersion;
        }
        catch (IOException ioe) {}
        return false;
    }

    private int parseMajorJavaVersion(String content) {
        final String versionRegex = "version \"(.*)\"";
        Pattern p = Pattern.compile(versionRegex);
        Matcher m = p.matcher(content);
        if (!m.find()) {
            return 0;
        }
        String version = m.group(1);

        // Ignore '1.' prefix for legacy Java versions
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }

        // Extract the major version number.
        final String numberRegex = "\\d+";
        p = Pattern.compile(numberRegex);
        m = p.matcher(version);
        if (!m.find()) {
            return 0;
        }
        return Integer.parseInt(m.group());
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProcessStreamConnectionProvider)) {
            return false;
        }
        ProcessStreamConnectionProvider other = (ProcessStreamConnectionProvider) obj;
        return Objects.equals(this.getCommands(), other.getCommands())
                && Objects.equals(this.getWorkingDirectory(), other.getWorkingDirectory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getCommands(), this.getWorkingDirectory());
    }

    @Override
    public String toString() {
        return "ProcessStreamConnectionProvider [commands=" + this.getCommands() + ", workingDir=" //$NON-NLS-1$//$NON-NLS-2$
                + this.getWorkingDirectory() + "]"; //$NON-NLS-1$
    }

}
