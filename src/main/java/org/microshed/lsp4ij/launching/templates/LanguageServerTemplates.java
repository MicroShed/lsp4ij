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
package org.microshed.lsp4ij.launching.templates;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Language server templates which hosts language server templates.
 */
public class LanguageServerTemplates {

    protected List<LanguageServerTemplate> templates;

    public @NotNull List<LanguageServerTemplate> getTemplates() {
        if (templates == null) {
            templates = new ArrayList<>();
        }
        return templates;
    }
}
