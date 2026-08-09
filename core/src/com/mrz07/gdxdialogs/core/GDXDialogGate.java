/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mrz07.gdxdialogs.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enforces the invariant that at most one native dialog is visible at any time.
 *
 * A single user input (e.g. a double-delivered touch event or a rapid double-tap)
 * must never result in stacked native dialogs. Every platform dialog claims this
 * gate immediately before it becomes visible and releases it when it is dismissed
 * (confirm, cancel, back key, system dismiss).
 *
 * The claim happens on the same thread that shows the dialog (Android/desktop UI
 * thread, iOS main thread), which is serialized with the dismissal callbacks, so
 * the atomic flag only guards cross-thread visibility.
 */
public final class GDXDialogGate {

	private static final AtomicBoolean visible = new AtomicBoolean(false);

	private GDXDialogGate() {
	}

	/** Claims the gate. Returns {@code false} while another dialog is already visible. */
	public static boolean tryClaim() {
		return visible.compareAndSet(false, true);
	}

	/** Releases the gate so the next dialog may be shown. */
	public static void release() {
		visible.set(false);
	}

	/** Returns whether a dialog is currently claimed (visible or about to be shown). */
	public static boolean isClaimed() {
		return visible.get();
	}
}
