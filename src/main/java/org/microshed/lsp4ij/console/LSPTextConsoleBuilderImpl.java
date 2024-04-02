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
package org.microshed.lsp4ij.console;

import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.microshed.lsp4ij.server.definition.LanguageServerDefinition;
import org.jetbrains.annotations.NotNull;

/**
 * Extends {@link TextConsoleBuilderImpl} to create the custom {@link LSPConsoleView}.
 */
public class LSPTextConsoleBuilderImpl extends TextConsoleBuilderImpl {

    private final LanguageServerDefinition serverDefinition;

    public LSPTextConsoleBuilderImpl(@NotNull LanguageServerDefinition serverDefinition, @NotNull Project project) {
        super(project);
        this.serverDefinition = serverDefinition;
    }

    @Override
    protected @NotNull ConsoleView createConsole() {
        return new LSPConsoleView(serverDefinition, getProject(), getScope(), isViewer(), isUsePredefinedMessageFilter());
    }
}
