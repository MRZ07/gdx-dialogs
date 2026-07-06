/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mrz07.gdxdialogs.html;

import com.mrz07.gdxdialogs.core.GDXDialogs;
import com.mrz07.gdxdialogs.core.dialogs.GDXButtonDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXProgressDialog;
import com.mrz07.gdxdialogs.core.dialogs.GDXTextPrompt;
import com.mrz07.gdxdialogs.html.dialogs.HTMLGDXButtonDialog;
import com.mrz07.gdxdialogs.html.dialogs.HTMLGDXProgressDialog;
import com.mrz07.gdxdialogs.html.dialogs.HTMLGDXTextPrompt;

public class HTMLGDXDialogs extends GDXDialogs {

    public HTMLGDXDialogs() {
        registerDialog(GDXButtonDialog.class, HTMLGDXButtonDialog::new);
        registerDialog(GDXProgressDialog.class, HTMLGDXProgressDialog::new);
        registerDialog(GDXTextPrompt.class, HTMLGDXTextPrompt::new);
    }

}
