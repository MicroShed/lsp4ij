/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.microshed.lsp4ij.commands.editor;

import com.google.gson.JsonArray;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.microshed.lsp4ij.JSONUtils;
import org.microshed.lsp4ij.commands.LSPCommand;
import org.microshed.lsp4ij.commands.LSPCommandAction;
import org.microshed.lsp4ij.usages.LSPUsagesManager;
import org.eclipse.lsp4j.Location;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Emulates Visual Studio Code's "editor.action.showReferences" command, to show the LSP references in a popup.
 */
public class ShowReferencesAction extends LSPCommandAction {

    @Override
    protected void commandPerformed(@NotNull LSPCommand command, @NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // "editor.action.showReferences" command which have 3 arguments:
        // - URI
        // - Position
        // - List of Location

        // Here a sample of "editor.action.showReferences" command:
        /*
          "command": {
            "title": "3 references",
            "command": "editor.action.showReferences",
            "arguments": [
              "file:///c%3A/.../test.tsx",
              {
                "line": 0,
                "character": 9
              },
              [
                {
                  "uri": "file:///c%3A/.../test.tsx",
                  "range": {
                    "start": {
                      "line": 5,
                      "character": 8
                    },
                    "end": {
                      "line": 5,
                      "character": 11
                    }
                  }
                }
              ]
            ]
          }
         */

        // Get the third argument (List of Location)
        JsonArray array = (JsonArray) command.getArgumentAt(2);
        if (array == null) {
            return;
        }
        // Get LSP4J Location from the JSON locations array
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            locations.add(JSONUtils.toModel(array.get(i), Location.class));
        }
        DataContext dataContext = e.getDataContext();
        // Call "Find Usages" in popup mode.
        LSPUsagesManager.getInstance(project).findShowUsagesInPopup(locations, dataContext, (MouseEvent) e.getInputEvent());
    }

    @Override
    protected @NotNull ActionUpdateThread getCommandPerformedThread() {
        return ActionUpdateThread.EDT;
    }
}
