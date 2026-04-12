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

import com.mrz07.gdxdialogs.core.dialogs.FallbackGDXButtonDialog;
import com.mrz07.gdxdialogs.core.dialogs.FallbackGDXProgressDialog;
import com.mrz07.gdxdialogs.core.dialogs.FallbackGDXTextPrompt;
import com.mrz07.gdxdialogs.core.dialogs.GDXButtonDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXTextPrompt;

class FallbackGDXDialogs extends GDXDialogs {

	public FallbackGDXDialogs() {
		registerDialog(GDXButtonDialog.class.getName(), FallbackGDXButtonDialog.class.getName());
		registerDialog(GDXProgressDialog.class.getName(), FallbackGDXProgressDialog.class.getName());
		registerDialog(GDXTextPrompt.class.getName(), FallbackGDXTextPrompt.class.getName());
	}

}
