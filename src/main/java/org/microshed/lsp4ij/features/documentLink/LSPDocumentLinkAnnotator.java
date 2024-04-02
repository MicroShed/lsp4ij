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
package org.microshed.lsp4ij.features.documentLink;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.microshed.lsp4ij.LSPFileSupport;
import org.microshed.lsp4ij.LSPIJUtils;
import org.microshed.lsp4ij.internal.CompletableFutures;
import org.microshed.lsp4ij.LanguageServersRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.microshed.lsp4ij.internal.CompletableFutures.waitUntilDone;

/**
 * Intellij {@link ExternalAnnotator} implementation which collect LSP document links and display them with underline style.
 */
public class LSPDocumentLinkAnnotator extends ExternalAnnotator<List<DocumentLinkData>, List<DocumentLinkData>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LSPDocumentLinkAnnotator.class);

    @Nullable
    @Override
    public List<DocumentLinkData> collectInformation(@NotNull PsiFile psiFile, @NotNull Editor editor, boolean hasErrors) {
        if (!LanguageServersRegistry.getInstance().isFileSupported(psiFile)) {
            return null;
        }
        // Consume LSP 'textDocument/documentLink' request
        LSPDocumentLinkSupport documentLinkSupport = LSPFileSupport.getSupport(psiFile).getDocumentLinkSupport();
        documentLinkSupport.cancel();
        CompletableFuture<List<DocumentLinkData>> documentLinkFuture = documentLinkSupport.getDocumentLinks();
        try {
            CompletableFutures.waitUntilDone(documentLinkFuture, psiFile);
        } catch (ProcessCanceledException | CancellationException e) {
            // cancel the LSP requests textDocument/documentLink
            documentLinkSupport.cancel();
            return null;
        } catch (ExecutionException e) {
            LOGGER.error("Error while consuming LSP 'textDocument/documentLink' request", e);
            return null;
        }

        if (CompletableFutures.isDoneNormally(documentLinkFuture)) {
            return documentLinkFuture.getNow(null);
        }
        return null;
    }

    @Override
    public @Nullable List<DocumentLinkData> doAnnotate(List<DocumentLinkData> documentLinks) {
        return documentLinks;
    }

    @Override
    public void apply(@NotNull PsiFile file, @NotNull List<DocumentLinkData> documentLinks, @NotNull AnnotationHolder holder) {
        if (documentLinks == null || documentLinks.isEmpty()) {
            return;
        }
        Document document = LSPIJUtils.getDocument(file.getVirtualFile());
        for (var documentLink : documentLinks) {
            TextRange range = LSPIJUtils.toTextRange(documentLink.documentLink().getRange(), document);
            holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY)
                    .range(range)
                    .textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
                    .create();
        }
    }
}
