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
        testGateRecoversAfterLifecycleFailures();

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

    /**
     * A dialog that never attaches must not keep the gate, or every later dialog in the process
     * would be dropped. Android releases the claim when a show is rejected; the release path is
     * platform independent, so the contract is checked here.
     */
    private static void testGateRecoversAfterLifecycleFailures() {
        final boolean[] failureNotified = {false};
        Object unavailable = GDXDialogGate.runClaimed(false, () -> {
            throw new AssertionError("unavailable host must not create a dialog");
        }, dialog -> {
            throw new AssertionError("unavailable host must not show a dialog");
        }, null, failure -> {
            check(failure == null, "unavailable host reports no runtime exception");
            failureNotified[0] = true;
        });
        check(unavailable == null, "unavailable host returns no dialog");
        check(failureNotified[0], "unavailable host invokes onFailure");
        check(!GDXDialogGate.isClaimed(), "unavailable host does not claim the gate");

        final RuntimeException creationFailure = new IllegalStateException("create failed");
        failureNotified[0] = false;
        Object creationResult = GDXDialogGate.runClaimed(true, () -> {
            throw creationFailure;
        }, dialog -> {
            throw new AssertionError("failed creation must not show a dialog");
        }, null, failure -> {
            check(failure == creationFailure, "creation failure is reported to onFailure");
            failureNotified[0] = true;
        });
        check(creationResult == null, "creation failure returns no dialog");
        check(failureNotified[0], "creation failure invokes onFailure");
        check(!GDXDialogGate.isClaimed(), "creation failure releases the gate");

        final RuntimeException showFailure = new IllegalArgumentException("show failed");
        failureNotified[0] = false;
        Object showResult = GDXDialogGate.runClaimed(true, Object::new, dialog -> {
            throw showFailure;
        }, null, failure -> {
            check(failure == showFailure, "show failure is reported to onFailure");
            failureNotified[0] = true;
        });
        check(showResult == null, "show failure returns no dialog");
        check(failureNotified[0], "show failure invokes onFailure");
        check(!GDXDialogGate.isClaimed(), "show failure releases the gate");

        Object shown = new Object();
        Object subsequent = GDXDialogGate.runClaimed(true, () -> shown, dialog -> { }, null, null);
        check(subsequent == shown, "a later dialog can claim and show after failures");
        check(GDXDialogGate.isClaimed(), "successful show retains the claim until dismiss");
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
