package com.mrz07.gdxdialogs.desktop.test;

import com.badlogic.gdx.Gdx;
import com.mrz07.gdxdialogs.core.GDXDialogGate;
import com.mrz07.gdxdialogs.core.GDXDialogs;
import com.mrz07.gdxdialogs.core.GDXDialogsSystem;
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXTextPrompt;
import com.mrz07.gdxdialogs.desktop.DesktopGDXDialogs;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Verifies that the single-dialog invariant enforced by {@link GDXDialogGate} holds.
 *
 * Run via: ./gradlew :desktop:runGateTest
 */
public class GDXDialogGateTest {

    public static void main(String[] args) throws Exception {
        testGateSemantics();

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[GateTest] headless environment — skipping Swing integration checks.");
        } else {
            testNoStackingWithSwingDialogs();
        }
        System.out.println("[GateTest] All checks passed.");
    }

    private static void testGateSemantics() {
        check(GDXDialogGate.tryClaim(), "first claim must succeed");
        check(!GDXDialogGate.tryClaim(), "second claim must be rejected");
        check(GDXDialogGate.isClaimed(), "gate must be claimed after a successful claim");
        GDXDialogGate.release();
        check(!GDXDialogGate.isClaimed(), "gate must be released after release()");
        check(GDXDialogGate.tryClaim(), "claim must succeed again after release");
        GDXDialogGate.release();
    }

    private static void testNoStackingWithSwingDialogs() throws Exception {
        // The desktop dialogs branch on os.name (macOS uses the non-dismissable osascript
        // path). Force the Swing implementation so the test can drive show()/dismiss().
        System.setProperty("os.name", "Generic");

        Gdx.app = new DesktopDialogTest.StubApplication();
        GDXDialogs dialogs = GDXDialogsSystem.install(new DesktopGDXDialogs());

        // A visible dialog must hold the gate.
        GDXProgressDialog progress = dialogs.newDialog(GDXProgressDialog.class);
        progress.setTitle("GateTest").setMessage("blocking").build().show();
        flushEdt();
        check(GDXDialogGate.isClaimed(), "a visible dialog must hold the gate");

        // A second show() while the first dialog is visible must be dropped.
        GDXTextPrompt prompt = dialogs.newDialog(GDXTextPrompt.class);
        prompt.setTitle("GateTest").setMessage("must not appear").build().show();
        flushEdt();
        check(GDXDialogGate.isClaimed(), "a dropped show() must not release the gate");

        // Dismissing the visible dialog must release the gate.
        progress.dismiss();
        flushEdt();
        check(!GDXDialogGate.isClaimed(), "dismissing the visible dialog must release the gate");

        // The next show() is accepted again.
        prompt.show();
        flushEdt();
        check(GDXDialogGate.isClaimed(), "show() after a dismissal must claim the gate again");
        prompt.dismiss();
        flushEdt();
        check(!GDXDialogGate.isClaimed(), "dismiss must release the gate again");
    }

    private static void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("[GateTest] FAILED: " + message);
        }
        System.out.println("[GateTest] OK: " + message);
    }
}
