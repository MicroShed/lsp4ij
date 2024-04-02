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
package org.microshed.lsp4ij.settings;

/**
 * Language server error reporting kind.
 */
public enum ErrorReportingKind {
    none, // ignore language server errors
    as_notification, // report error in an IJ notification
    in_log; // report error in the log

    public static ErrorReportingKind getDefaultValue() {
        return as_notification;
    }
}
