//
// Package documentation for com.openbravo.pos.forms
// Application Shell & Desktop UI Architecture
//

/**
 * Core Application Shell, Window Management, and Navigation Infrastructure for KriolOS POS.
 *
 * <p>This package implements the <b>Application Shell Pattern</b> for the POS desktop client,
 * adhering to industry-standard UI/UX architectural concepts for modular desktop applications:
 *
 * <h2>Architectural Components & Responsibilities</h2>
 *
 * <h3>1. Window Shell — {@link com.openbravo.pos.forms.WindowShell}</h3>
 * <ul>
 *   <li><b>Role:</b> The root operating system window container (subclasses {@link javax.swing.JFrame}).</li>
 *   <li><b>Responsibilities:</b> Desktop window lifecycle, window decorations, fullscreen and maximized state,
 *       multi-monitor placement, application title branding, and top-level {@link javax.swing.JLayeredPane}
 *       hosting modal glasspane overlays.</li>
 * </ul>
 *
 * <h3>2. Application Shell — {@link com.openbravo.pos.forms.ApplicationShell}</h3>
 * <ul>
 *   <li><b>Role:</b> The primary application controller and outer UI shell (subclasses {@link javax.swing.JPanel}
 *       and implements {@link com.openbravo.pos.forms.AppView}).</li>
 *   <li><b>Responsibilities:</b> Application session lifecycle, database connection management, hardware device
 *       subsystem (printers, fiscal devices, barcode scanners, customer displays), UI theme provider integration,
 *       and primary view switching using {@link java.awt.CardLayout} across primary phases:
 *       <ol>
 *         <li><b>Authentication Phase:</b> {@link com.openbravo.pos.forms.components.AuthenticationPanel} for user login and database selection.</li>
 *         <li><b>Workspace Phase:</b> {@link com.openbravo.pos.forms.WordspacePanel} once authenticated.</li>
 *       </ol>
 *   </li>
 * </ul>
 *
 * <h3>3. Workspace Shell — {@link com.openbravo.pos.forms.WordspacePanel}</h3>
 * <ul>
 *   <li><b>Role:</b> The authenticated user workspace container.</li>
 *   <li><b>Responsibilities:</b> Task navigation, hierarchical menu layout, session status bar, breadcrumb/history
 *       navigation (forward/back), user role permissions filtering, and view swapping for functional POS panels
 *       (Sales, Inventory, Customers, Maintenance, Reporting).</li>
 * </ul>
 *
 * <h3>4. Application Bootstrap — {@link com.openbravo.pos.forms.BootstrapPOS}</h3>
 * <ul>
 *   <li><b>Role:</b> Desktop application bootstrap entry point ({@code main} method).</li>
 *   <li><b>Responsibilities:</b> JVM configuration, theme initialization, single-instance verification, and launch of {@link com.openbravo.pos.forms.WindowShell}.</li>
 * </ul>
 *
 * <h2>Auxiliary Components</h2>
 * <p>Reusable sub-panels and leaf views are organized under {@link com.openbravo.pos.forms.components}:</p>
 * <ul>
 *   <li>{@link com.openbravo.pos.forms.components.AuthenticationPanel}: Integrated operator authentication and database selection view.</li>
 *   <li>{@link com.openbravo.pos.forms.components.CopyrightPanel}: Legal attribution and copyright branding view.</li>
 *   <li>{@link com.openbravo.pos.forms.components.ErrorPanel}: Fallback placeholder view rendered when view bean creation fails.</li>
 * </ul>
 */
package com.openbravo.pos.forms;
