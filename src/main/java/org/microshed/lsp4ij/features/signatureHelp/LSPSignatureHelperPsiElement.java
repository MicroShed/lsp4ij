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
package org.microshed.lsp4ij.features.signatureHelp;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.microshed.lsp4ij.features.LSPPsiElement;
import org.eclipse.lsp4j.SignatureHelp;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * LSP {@link PsiElement} implementation which stores the file
 * and the offset where the signature help has been invoked in an editor.
 */
public class LSPSignatureHelperPsiElement extends LSPPsiElement {

    private SignatureHelp activeSignatureHelp;
    public LSPSignatureHelperPsiElement(@NotNull PsiFile file, @NotNull TextRange textRange) {
        super(file, textRange);
    }

    public SignatureHelp getActiveSignatureHelp() {
        return activeSignatureHelp;
    }

    public void setActiveSignatureHelp(SignatureHelp activeSignatureHelp) {
        this.activeSignatureHelp = activeSignatureHelp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LSPSignatureHelperPsiElement that = (LSPSignatureHelperPsiElement) o;
        return Objects.equals(getContainingFile(), that.getContainingFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainingFile());
    }
}