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
package org.microshed.lsp4ij.features;

/**
 * LSP request constants.
 */
public class LSPRequestConstants {

    public static final String TEXT_DOCUMENT_DECLARATION = "textDocument/declaration";
    public static final String TEXT_DOCUMENT_DEFINITION = "textDocument/definition";
    public static final String TEXT_DOCUMENT_DOCUMENT_LINK = "textDocument/documentLink";
    public static final String TEXT_DOCUMENT_FOLDING_RANGE = "textDocument/foldingRange";
    public static final String TEXT_DOCUMENT_TYPE_DEFINITION = "textDocument/typeDefinition";
    public static final String TEXT_DOCUMENT_CODE_LENS = "textDocument/codeLens";
    public static final String TEXT_DOCUMENT_RESOLVE_CODE_LENS = "textDocument/resolveCodelens";
    public static final String TEXT_DOCUMENT_HOVER = "textDocument/hover";
    public static final String TEXT_DOCUMENT_INLAY_HINT = "textDocument/inlayHint";
    public static final String TEXT_DOCUMENT_RESOLVE_INLAY_HINT = "textDocument/resolveInlayHint";
    public static final String TEXT_DOCUMENT_DOCUMENT_COLOR = "textDocument/documentColor";
    public static final String TEXT_DOCUMENT_COMPLETION = "textDocument/completion";
    public static final String TEXT_DOCUMENT_DOCUMENT_HIGHLIGHT = "textDocument/documentHighlight";
    public static final String TEXT_DOCUMENT_FORMATTING = "textDocument/formatting";
    public static final String TEXT_DOCUMENT_RANGE_FORMATTING = "textDocument/rangeFormatting";
    public static final String TEXT_DOCUMENT_IMPLEMENTATION = "textDocument/implementation";
    public static final String TEXT_DOCUMENT_REFERENCES = "textDocument/references";
    public static final String TEXT_DOCUMENT_SIGNATURE_HELP = "textDocument/signatureHelp";


    private LSPRequestConstants() {

    }
}
