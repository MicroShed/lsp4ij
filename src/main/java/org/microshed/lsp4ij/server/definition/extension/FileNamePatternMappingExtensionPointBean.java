/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc. and others.
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
package org.microshed.lsp4ij.server.definition.extension;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.RequiredElement;
import com.intellij.serviceContainer.BaseKeyedLazyInstance;
import com.intellij.util.xmlb.annotations.Attribute;
import org.microshed.lsp4ij.DocumentMatcher;
import org.eclipse.lsp4j.TextDocumentItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.microshed.lsp4ij.ExtensionPointBeanConstants;

/**
 * File type mapping extension point.
 *
 * <pre>
 *   <extensions defaultExtensionNs="com.redhat.devtools.lsp4ij">
 *
 *     <fileNamePatternMapping
 *                      patterns="*.css;*.less"
 *                      serverId="myLanguageServerId"
 *                      languageId="css" />
 *   </extensions>
 * </pre>
 */
public class FileNamePatternMappingExtensionPointBean extends BaseKeyedLazyInstance<DocumentMatcher> {

    private static final DocumentMatcher DEFAULT_DOCUMENT_MATCHER = (file,project) -> true;

    public static final ExtensionPointName<FileNamePatternMappingExtensionPointBean> EP_NAME = ExtensionPointName.create(ExtensionPointBeanConstants.getFileNamePatternMappingExtensionName());

    /**
     * The language server mapped with the language {@link #patterns file name patterns}.
     */
    @Attribute("serverId")
    @RequiredElement
    public String serverId;

    /**
     * Semicolon-separated list of patterns (strings containing ? and * characters) to be associated with the language server {@link #serverId server id}.
     */
    @Attribute("patterns")
    @RequiredElement
    public String patterns;

    /**
     * The LSP language ID which must be used in the LSP {@link TextDocumentItem#getLanguageId()}. If it is not defined
     * the languageId used will be the IntelliJ {#link language}.
     */
    @Attribute("languageId")
    public String languageId;

    /**
     * The {@link DocumentMatcher document matcher}.
     */
    @Attribute("documentMatcher")
    public String documentMatcher;

    public @NotNull DocumentMatcher getDocumentMatcher() {
        try {
            return super.getInstance();
        }
        catch(Exception e) {
            return DEFAULT_DOCUMENT_MATCHER;
        }
    }

    @Override
    protected @Nullable String getImplementationClassName() {
        return documentMatcher;
    }
}
