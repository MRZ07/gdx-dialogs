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

package com.mrz07.gdxdialogs.android;

import android.app.Activity;
import android.app.Dialog;

import com.badlogic.gdx.Gdx;

import com.mrz07.gdxdialogs.core.GDXDialogGate;
import com.mrz07.gdxdialogs.core.GDXDialogsVars;

/**
 * Shows a native dialog without letting a dead window token take the process down.
 *
 * A dialog is posted to the UI thread and shown later; the Activity can be gone by then, and
 * {@link Dialog#show()} then throws {@code WindowManager.BadTokenException}. That used to crash
 * the app and, because {@link GDXDialogGate} is released on dismiss only, also left the gate
 * claimed so every later dialog in the process was silently refused.
 */
public final class AndroidDialogGate {

	private AndroidDialogGate() {
	}

	/**
	 * Shows {@code dialog}, or releases the gate and runs {@code onFailure} when it cannot attach.
	 *
	 * Must be called on the UI thread after the gate was claimed. Returns whether the dialog is
	 * visible, so callers can drop their own state when it is not.
	 */
	public static boolean show(Activity activity, Dialog dialog, String owner, Runnable onFailure) {
		if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
			return drop(owner, "activity is gone", null, onFailure);
		}
		try {
			dialog.show();
			return true;
		} catch (RuntimeException failure) {
			return drop(owner, "window token rejected", failure, onFailure);
		}
	}

	private static boolean drop(String owner, String reason, RuntimeException failure, Runnable onFailure) {
		GDXDialogGate.release();
		if (Gdx.app != null) {
			Gdx.app.error(GDXDialogsVars.LOG_TAG, owner + " not shown: " + reason, failure);
		}
		if (onFailure != null) {
			onFailure.run();
		}
		return false;
	}
}
