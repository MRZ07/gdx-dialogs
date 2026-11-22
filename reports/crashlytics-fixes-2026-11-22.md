# Crashlytics library fix — 2026-11-22

## Status and authorization

The user asked for this library to be fixed and released, then for every finding to
be fixed in the best way. Releasing it (tag and push to JitPack) was explicitly
authorized for this repository only.

One fleet finding belongs here: `samslevelmaker/android/5d25dc46…`,
`android.view.WindowManager$BadTokenException` in
`AndroidGDXTextPrompt$1.run`, FATAL.

## Root cause

`show()` posted a `Runnable` to the UI thread that built and showed the dialog. By
the time it ran, the Activity could already be finishing or destroyed, and
`Dialog.show()` then threw `WindowManager.BadTokenException` — on the UI thread, so
it crashed the process.

The same code also released the shared `GDXDialogGate` only from `OnDismissListener`.
A dialog that never attached therefore never dismissed, so the gate stayed claimed
for the rest of the process and every later dialog was silently dropped. One
rejected window token disabled all dialogs.

## The change

`AndroidDialogGate.show(activity, dialog, owner, onFailure)` is now the single show
path for all three Android dialogs (`TextPrompt`, `ButtonDialog`, `ProgressDialog`):

- returns without touching the dialog when the Activity is null, finishing or
  destroyed;
- catches any `RuntimeException` from the window manager, so a rejected token is a
  logged drop rather than a crash;
- releases `GDXDialogGate` on every failure, so a failed show cannot disable later
  dialogs;
- runs the caller's cancel path on the app thread, so `TextPromptListener.cancel`
  and `ButtonClickListener.click(-1)` fire and no continuation waits on a dialog
  that will never appear.

## Release

| Tag | Commit | Contents |
|---|---|---|
| `v2.1.1` | `5555415` | `AndroidDialogGate`, all three dialogs routed through it, cancel callbacks on failure, desktop gate-recovery check |

## Verification

| Check | Result |
|---|---|
| `:android:compileReleaseJavaWithJavac` (JDK 21, AGP with compileSdk 35) | ok |
| `:desktop:runGateTest` | ok, including the new "gate recovers after a failed show" check |
| JitPack `v2.1.1` | build `ok`, JDK 17, all five modules published |
| Consumers | all seven bumped to `dialogsVersion=2.1.1`; `:android:assembleRelease` ok in each |

The Android path itself is not exercised by a test: the rejection needs a real
window token. The gate contract it depends on — claim, release, claim again — is
covered by the desktop test, and the Activity check is a straight guard on
`isFinishing()`/`isDestroyed()`.

## Residuals

- `AndroidGDXProgressDialog` passes no failure callback; a progress dialog that
  cannot attach is logged and its gate released, but any caller waiting for its
  dismiss listener keeps waiting. Progress dialogs are dismissed by the app, which
  now sees no dialog to dismiss, so this is a no-op rather than a hang.
- Only the Android backend is guarded. The desktop, iOS and HTML backends release
  the gate through their own dismiss paths and were not changed.
