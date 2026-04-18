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

package com.mrz07.gdxdialogs.android.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;

import com.mrz07.gdxdialogs.core.GDXDialogsVars;
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;

public class AndroidGDXProgressDialog implements GDXProgressDialog {

	private Activity activity;

	private AlertDialog progressDialog;

	private CharSequence message = "";
	private CharSequence title = "";
	private boolean cancelable = false;

	private boolean isBuild = false;

	public AndroidGDXProgressDialog(Activity activity) {
		this.activity = activity;
	}

	@Override
	public GDXProgressDialog setMessage(CharSequence message) {
		this.message = message;
		return this;
	}

	@Override
	public GDXProgressDialog setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	@Override
	public GDXProgressDialog show() {
		if (!isBuild) {
			throw new RuntimeException(AndroidGDXProgressDialog.class.getSimpleName() + " has not been build. Use"+
					" build() before show().");
		}

		// Create Views and the dialog here on the UI thread, together with show().
		// Doing this in build() required blocking the calling thread (Thread.sleep)
		// which causes a deadlock/ANR when build() is called from the GL thread
		// while it holds the DefaultAndroidInput lock.
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (activity.isFinishing() || activity.isDestroyed()) return;
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(title);
				builder.setCancelable(cancelable);

				ProgressBar progressBar = new ProgressBar(activity);
				progressBar.setIndeterminate(true);
				int padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
				progressBar.setPadding(padding, padding, padding, padding);

				if (message != null && message.length() > 0) {
					android.widget.LinearLayout layout = new android.widget.LinearLayout(activity);
					layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
					layout.setPadding(padding, padding, padding, padding);
					layout.addView(progressBar);
					TextView messageView = new TextView(activity);
					messageView.setText(message);
					messageView.setPadding(padding, 0, 0, 0);
					layout.addView(messageView);
					builder.setView(layout);
				} else {
					builder.setView(progressBar);
				}

				progressDialog = builder.create();
				Gdx.app.debug(GDXDialogsVars.LOG_TAG, GDXProgressDialog.class.getSimpleName() + " now shown.");
				progressDialog.show();
			}
		});

		return this;
	}

	@Override
	public GDXProgressDialog dismiss() {

		if (!isBuild) {
			throw new RuntimeException(AndroidGDXProgressDialog.class.getSimpleName() + " has not been build. Use "+
					"build() before dismiss().");
		}

		if (progressDialog != null) {
			Gdx.app.debug(GDXDialogsVars.LOG_TAG, GDXProgressDialog.class.getSimpleName() + " dismissed.");
			progressDialog.dismiss(); // Method is thread safe.
		}

		return this;
	}

	@Override
	public GDXProgressDialog setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
		return this;
	}

	@Override
	public GDXProgressDialog build() {
		// View and dialog creation is deferred to show() where it runs on the UI
		// thread together with the actual show call. This ensures build() is
		// non-blocking and safe to call from the GL thread.
		isBuild = true;
		return this;
	}

}
