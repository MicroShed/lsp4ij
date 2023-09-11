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
package org.eclipse.lsp4ij.operations.diagnostics;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.eclipse.lsp4ij.LSPIJUtils;
import org.eclipse.lsp4ij.LSPVirtualFileWrapper;
import org.eclipse.lsp4ij.operations.codeactions.LSPLazyCodeActionIntentionAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Intellij {@link ExternalAnnotator} implementation which get the current LSP diagnostics for a given file and translate
 * them into Intellij {@link com.intellij.lang.annotation.Annotation}.
 */
public class LSPDiagnosticAnnotator extends ExternalAnnotator<LSPVirtualFileWrapper, LSPVirtualFileWrapper> {

	@Nullable
	@Override
	public LSPVirtualFileWrapper collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
		try {
			return LSPVirtualFileWrapper.getLSPVirtualFileWrapper(file.getVirtualFile());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public @Nullable LSPVirtualFileWrapper doAnnotate(LSPVirtualFileWrapper wrapper) {
		return wrapper;
	}

	@Override
	public void apply(@NotNull PsiFile file, LSPVirtualFileWrapper wrapper, @NotNull AnnotationHolder holder) {
		// Get current LSP diagnostics of the current file
		final Collection<LSPDiagnosticsForServer> diagnosticsPerServer = wrapper.getAllDiagnostics();
		Document document = LSPIJUtils.getDocument(file.getVirtualFile());

		// Loop for language server which report diagnostics for the given file
		for (var ds :
				diagnosticsPerServer) {
			// Loop for LSP diagnostics to transform it to Intellij annotation.
			for (Diagnostic diagnostic : ds.getDiagnostics()) {
				ProgressManager.checkCanceled();
				createAnnotation(diagnostic, document, ds, holder);
			}
		}
	}

	private static void createAnnotation(Diagnostic diagnostic, Document document, LSPDiagnosticsForServer diagnosticsForServer, AnnotationHolder holder) {
		TextRange range = LSPIJUtils.toTextRange(diagnostic.getRange(), document);
		if (range == null) {
			// Language server reports invalid diagnostic, ignore it.
			return;
		}
		// Collect information required to create Intellij Annotations
		HighlightSeverity severity = toHighlightSeverity(diagnostic.getSeverity());
		String message = diagnostic.getMessage();

		// Create Intellij Annotation from the given LSP diagnostic
		AnnotationBuilder builder = holder
				.newAnnotation(severity, message)
				.range(range);

		// Register lazy quick fixes
		List<LSPLazyCodeActionIntentionAction> fixes = diagnosticsForServer.getQuickFixesFor(diagnostic);
		for (IntentionAction fix : fixes) {
			builder.withFix(fix);
		}
		builder.create();
	}

	private static HighlightSeverity toHighlightSeverity(DiagnosticSeverity severity) {
		if (severity == null) {
			return HighlightSeverity.ERROR;
		}
		switch (severity) {
			case Warning:
				return HighlightSeverity.WEAK_WARNING;
			case Hint:
			case Information:
				return HighlightSeverity.INFORMATION;
			default:
				return HighlightSeverity.ERROR;
		}
	}

}