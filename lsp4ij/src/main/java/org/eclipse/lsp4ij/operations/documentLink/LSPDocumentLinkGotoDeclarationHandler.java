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
package org.eclipse.lsp4ij.operations.documentLink;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.eclipse.lsp4ij.LanguageServerBundle;
import org.eclipse.lsp4ij.LSPIJUtils;
import org.eclipse.lsp4ij.LSPVirtualFileWrapper;
import org.eclipse.lsp4j.DocumentLink;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link GotoDeclarationHandler} implementation used to open LSP document link with CTrl+Click.
 */
public class LSPDocumentLinkGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        Document document = editor.getDocument();
        VirtualFile file = LSPIJUtils.getFile(document);
        if (!LSPVirtualFileWrapper.hasWrapper(file)) {
            return PsiElement.EMPTY_ARRAY;
        }
        Module module = LSPIJUtils.getProject(file);
        Project project = module != null ? module.getProject() : null;
        if (project == null || project.isDisposed()) {
            return PsiElement.EMPTY_ARRAY;
        }
        LSPVirtualFileWrapper wrapper = LSPVirtualFileWrapper.getLSPVirtualFileWrapper(file);
        Collection<LSPDocumentLinkForServer> allLinks = wrapper.getAllDocumentLink();
        if (allLinks.isEmpty()) {
            return PsiElement.EMPTY_ARRAY;
        }
        // The file has some LSP document links
        for (LSPDocumentLinkForServer links : allLinks) {
            for (DocumentLink documentLink : links.getDocumentLinks()) {
                TextRange range = LSPIJUtils.toTextRange(documentLink.getRange(), document);
                if (range.contains(offset)) {
                    // The Ctrl+Click has been done in a LSP document link,try to open the document.
                    final String target = documentLink.getTarget();
                    if (target != null && !target.isEmpty()) {
                        VirtualFile targetFile = LSPIJUtils.findResourceFor(target);
                        if (targetFile == null) {
                            // The LSP document link file doesn't exist, open a file dialog
                            // which asks if user want to create the file.
                            // At this step we cannot open a dialog directly, we need to open the dialog
                            // with invoke later.
                            ApplicationManager.getApplication().invokeLater(() -> {
                                int result = Messages.showYesNoDialog(LanguageServerBundle.message("lsp.create.file.confirm.dialog.message", target),
                                        LanguageServerBundle.message("lsp.create.file.confirm.dialog.title"), Messages.getQuestionIcon());
                                if (result == Messages.YES) {
                                    try {
                                        // Create file
                                        VirtualFile newFile = LSPIJUtils.createFile(target);
                                        if (newFile != null) {
                                            // Open it in an editor
                                            LSPIJUtils.openInEditor(newFile, null, project);
                                        }
                                    } catch (IOException e) {
                                        Messages.showErrorDialog(LanguageServerBundle.message("lsp.create.file.error.dialog.message", target, e.getMessage()),
                                                LanguageServerBundle.message("lsp.create.file.error.dialog.title"));
                                    }
                                }
                            });
                            // Return an empty result here.
                            // If user accepts to create the file, the open is done after the creation of teh file.
                            return PsiElement.EMPTY_ARRAY;
                        }
                        return new PsiElement[]{PsiManager.getInstance(project).findFile(targetFile)};
                    }
                }
            }
        }
        return PsiElement.EMPTY_ARRAY;
    }
}
