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
package org.eclipse.lsp4ij.console.explorer.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.lsp4ij.LanguageServerBundle;
import org.eclipse.lsp4ij.LanguageServerWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Action to restart the selected language server process from the language explorer.
 */
public class RestartServerAction extends TreeAction implements DumbAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestartServerAction.class);//$NON-NLS-1$

    public static final String ACTION_ID = "org.eclipse.lsp4ij.actions.explorer.console.lsp4ij.RestartServerAction";

    public RestartServerAction() {
        super(LanguageServerBundle.message("lsp.console.explorer.actions.restart"));
    }

    @Override
    protected void actionPerformed(@NotNull Tree tree, @NotNull AnActionEvent e) {
        LanguageServerWrapper languageServer = getSelectedLanguageServer(tree);
        if (languageServer != null) {
            try {
                languageServer.restart();
            } catch (IOException ex) {
                LOGGER.error("Failed restarting server", ex);
            }
        }
    }
}
