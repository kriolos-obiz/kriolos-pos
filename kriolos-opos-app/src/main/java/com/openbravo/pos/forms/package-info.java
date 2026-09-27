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
 * <h3>1. Window Shell — {@link com.openbravo.pos.forms.RootFrame}</h3>
 * <ul>
 *   <li><b>Role:</b> The root operating system window container (subclasses {@link javax.swing.JFrame}).</li>
 *   <li><b>Responsibilities:</b> Desktop window lifecycle, window decorations, fullscreen and maximized state,
 *       multi-monitor placement, application title branding, and top-level {@link javax.swing.JLayeredPane}
 *       hosting modal glasspane overlays.</li>
 * </ul>
 *
 * <h3>2. Application Shell — {@link com.openbravo.pos.forms.RootAppPanel}</h3>
 * <ul>
 *   <li><b>Role:</b> The primary application controller and outer UI shell (subclasses {@link javax.swing.JPanel}
 *       and implements {@link com.openbravo.pos.forms.AppView}).</li>
 *   <li><b>Responsibilities:</b> Application session lifecycle, database connection management, hardware device
 *       subsystem (printers, fiscal devices, barcode scanners, customer displays), UI theme provider integration,
 *       and primary view switching using {@link java.awt.CardLayout} across three primary phases:
 *       <ol>
 *         <li><b>Splash Phase:</b> {@link com.openbravo.pos.forms.SplashScreenPanel} during boot and migrations.</li>
 *         <li><b>Authentication Phase:</b> {@link com.openbravo.pos.forms.AuthPanel} for user login and database selection.</li>
 *         <li><b>Workspace Phase:</b> {@link com.openbravo.pos.forms.PrincipalAppPanel} once authenticated.</li>
 *       </ol>
 *   </li>
 * </ul>
 *
 * <h3>3. Workspace Shell — {@link com.openbravo.pos.forms.PrincipalAppPanel}</h3>
 * <ul>
 *   <li><b>Role:</b> The authenticated user workspace container.</li>
 *   <li><b>Responsibilities:</b> Task navigation, hierarchical menu layout, session status bar, breadcrumb/history
 *       navigation (forward/back), user role permissions filtering, and view swapping for functional POS panels
 *       (Sales, Inventory, Customers, Maintenance, Reporting).</li>
 * </ul>
 *
 * <h3>4. Authentication View — {@link com.openbravo.pos.forms.AuthPanel}</h3>
 * <ul>
 *   <li><b>Role:</b> Operator authentication and session entry view.</li>
 *   <li><b>Responsibilities:</b> User avatar button grid, password/PIN capture via modal dialogues,
 *       and embedded database switching via {@link com.openbravo.pos.forms.DatabaseSelectorPanel}.</li>
 * </ul>
 *
 * <h3>5. Supporting Shell Components</h3>
 * <ul>
 *   <li>{@link com.openbravo.pos.forms.DatabaseSelectorPanel}: Interactive panel for selecting and activating configured databases.</li>
 *   <li>{@link com.openbravo.pos.forms.SplashScreenPanel}: Initial loading splash view displayed during bootstrap.</li>
 *   <li>{@link com.openbravo.pos.forms.CopyrightPanel}: Legal attribution and copyright branding view.</li>
 *   <li>{@link com.openbravo.pos.forms.NullPanel}: Fallback placeholder view rendered when view bean creation fails.</li>
 *   <li>{@link com.openbravo.pos.forms.BootstrapPOS}: Desktop application bootstrap entry point ({@code main} method).</li>
 * </ul>
 *
 * <h2>Backwards Compatibility</h2>
 * <p>Legacy class names ({@code JRootFrame}, {@code JRootApp}, {@code JPrincipalApp}, {@code JAuthPanel},
 * {@code JCopyRightPanel}, {@code JPanelNull}, {@code JSplashScreen}, {@code DatabaseSelector}, and {@code StartPOS})
 * are retained as deprecated subclasses or delegating shims to preserve binary and source compatibility
 * across external modules and automation test harnesses without requiring modifications outside this package.</p>
 */
package com.openbravo.pos.forms;
