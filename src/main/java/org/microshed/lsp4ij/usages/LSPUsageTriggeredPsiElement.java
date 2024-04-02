/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.microshed.lsp4ij.usages;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.microshed.lsp4ij.features.LSPPsiElement;
import org.eclipse.lsp4j.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Psi element used to execute a "Find Usages".
 * This Psi element contains the offset of the caret editor where the
 * "Find Usages" has been triggered.
 */
public class LSPUsageTriggeredPsiElement extends LSPPsiElement {

    private List<Location> references;

    public LSPUsageTriggeredPsiElement(@NotNull PsiFile file, @NotNull TextRange textRange) {
        super(file, textRange);
    }

    @Nullable
    public List<Location> getLSPReferences() {
        return references;
    }

    public void setLSPReferences(List<Location> references) {
        this.references = references;
    }
}
