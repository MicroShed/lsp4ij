/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.microshed.lsp4ij.features.inlayhint;

import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.SequencePresentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import org.microshed.lsp4ij.LSPFileSupport;
import org.microshed.lsp4ij.LSPIJUtils;
import org.microshed.lsp4ij.LanguageServerItem;
import org.microshed.lsp4ij.features.AbstractLSPInlayHintsProvider;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * LSP textDocument/inlayHint support.
 */
public class LSPInlayHintsProvider extends AbstractLSPInlayHintsProvider {

    public LSPInlayHintsProvider() {

    }

    @Override
    protected void doCollect(@NotNull PsiFile psiFile,
                             @NotNull Editor editor,
                             @NotNull PresentationFactory factory,
                             @NotNull InlayHintsSink inlayHintsSink,
                             @NotNull List<CompletableFuture> pendingFutures) throws InterruptedException {
        // Get LSP inlay hints from cache or create them
        LSPInlayHintsSupport inlayHintSupport = LSPFileSupport.getSupport(psiFile).getInlayHintsSupport();
        Range viewPortRange = getViewPortRange(editor);
        InlayHintParams params = new InlayHintParams(LSPIJUtils.toTextDocumentIdentifier(psiFile.getVirtualFile()), viewPortRange);
        CompletableFuture<List<InlayHintData>> future = inlayHintSupport.getInlayHints(params);

        try {
            List<Pair<Integer, InlayHintData>> inlayHints = createInlayHints(editor.getDocument(), future);
            // Render inlay hints and collect all unfinished inlayHint/resolve futures
            inlayHints.stream()
                    .collect(Collectors.groupingBy(p -> p.first))
                    .forEach((offset, list) ->
                            inlayHintsSink.addInlineElement(
                                    offset,
                                    false,
                                    toPresentation(editor, list, factory, pendingFutures),
                                    false));
        } finally {
            if (!future.isDone()) {
                // the future which collects all textDocument/inlayHint for all servers is not finished
                // add it to the pending futures to refresh again the UI when this future will be finished.
                pendingFutures.add(future);
            }
        }
    }

    @NotNull
    private List<Pair<Integer, InlayHintData>> createInlayHints(Document document,
                                                                CompletableFuture<List<InlayHintData>> future) throws InterruptedException {
        List<Pair<Integer, InlayHintData>> inlayHints = new ArrayList<>();
        if (future.isDone()) {
            List<InlayHintData> data = future.getNow(Collections.emptyList());
            fillInlayHints(document, data, inlayHints);
        } else {
            while (!future.isDone()) {
                ProgressManager.checkCanceled();
                try {
                    List<InlayHintData> data = future.get(25, TimeUnit.MILLISECONDS);
                    fillInlayHints(document, data, inlayHints);
                } catch (TimeoutException | ExecutionException e) {
                    // Do nothing
                }
            }
        }
        return inlayHints;
    }

    private static void fillInlayHints(Document document, List<InlayHintData> data, List<Pair<Integer, InlayHintData>> inlayHints) {
        for (var inlayHintData : data) {
            int offset = LSPIJUtils.toOffset(inlayHintData.inlayHint().getPosition(), document);
            inlayHints.add(Pair.create(offset, inlayHintData));
        }
    }

    @NotNull
    private static Range getViewPortRange(Editor editor) {
        // LSP textDocument/inlayHint request parameter expects to fill the visible view port range.
        // As Intellij inlay hint provider is refreshed just only when editor is opened or editor content changed
        // and not when editor is scrolling, the view port range must be created with full text document offsets.
        Position start = new Position(0, 0);
        Document document = editor.getDocument();
        Position end = LSPIJUtils.toPosition(document.getTextLength(), document);
        return new Range(start, end);
    }

    private InlayPresentation toPresentation(@NotNull Editor editor,
                                             @NotNull List<Pair<Integer, InlayHintData>> elements,
                                             @NotNull PresentationFactory factory,
                                             @NotNull List<CompletableFuture> toResolve) {
        List<InlayPresentation> presentations = new ArrayList<>();
        elements.forEach(p -> {
            Either<String, List<InlayHintLabelPart>> label = p.second.inlayHint().getLabel();
            if (label.isLeft()) {
                presentations.add(factory.smallText(label.getLeft()));
            } else {
                int index = 0;
                for (InlayHintLabelPart part : label.getRight()) {
                    InlayPresentation text = createInlayPresentation(editor, factory, p, index, part);
                    if (part.getTooltip() != null && part.getTooltip().isLeft()) {
                        text = factory.withTooltip(part.getTooltip().getLeft(), text);
                    }
                    presentations.add(text);
                    index++;
                }
            }
        });
        return factory.roundWithBackground(new SequencePresentation(presentations));
    }

    @NotNull
    private InlayPresentation createInlayPresentation(
            Editor editor,
            PresentationFactory factory,
            Pair<Integer, InlayHintData> p,
            int index,
            InlayHintLabelPart part) {
        InlayPresentation text = factory.smallText(part.getValue());
        if (hasCommand(part)) {
            // InlayHintLabelPart defines a Command, create a clickable inlay hint
            int finalIndex = index;
            text = factory.referenceOnHover(text, (event, translated) ->
                    executeCommand(p.second.languageServer(), p.second.inlayHint(), finalIndex, event, editor)
            );
        }
        return text;
    }

    private static boolean hasCommand(InlayHintLabelPart part) {
        Command command = part.getCommand();
        return (command != null && command.getCommand() != null && !command.getCommand().isEmpty());
    }

    /**
     * Execute codeLens command.
     *
     * @param languageServer the language server.
     * @param inlayHint      the inlay hint.
     * @param index          the inlay part index where the command should be defined.
     * @param event         the Mouse event.
     * @param editor        the editor.
     */
    private void executeCommand(@NotNull LanguageServerItem languageServer,
                                @NotNull InlayHint inlayHint,
                                int index,
                                @Nullable MouseEvent event,
                                @NotNull Editor editor) {
        if (languageServer.isResolveInlayHintSupported()) {
            languageServer.getTextDocumentService()
                    .resolveInlayHint(inlayHint)
                    .thenAcceptAsync(resolvedInlayHint -> {
                                if (resolvedInlayHint != null) {
                                    executeClientCommand(getCommand(resolvedInlayHint, index), editor, event);
                                }
                            }
                    );
        } else {
            executeClientCommand(getCommand(inlayHint, index), editor, event);
        }
    }

    private static @Nullable Command getCommand(@Nullable InlayHint inlayHint, int index) {
        if (inlayHint == null) {
            return null;
        }
        if (inlayHint.getLabel() != null && inlayHint.getLabel().isRight()) {
            List<InlayHintLabelPart> parts = inlayHint.getLabel().getRight();
            if (index < parts.size()) {
                return parts.get(index).getCommand();
            }
        }
        return null;
    }

}
