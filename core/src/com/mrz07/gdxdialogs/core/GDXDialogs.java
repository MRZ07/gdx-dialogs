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

import java.util.HashMap;
import java.util.Map;

public abstract class GDXDialogs {

	private final Map<Class<?>, DialogFactory<?>> factories = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T newDialog(Class<T> cls) {
		DialogFactory<?> factory = factories.get(cls);
		if (factory != null) {
			return cls.cast(((DialogFactory<T>) factory).create());
		}
		throw new RuntimeException(cls.getName() + " is not registered.");
	}

	protected <T> void registerDialog(Class<T> interfaceClass, DialogFactory<T> factory) {
		factories.put(interfaceClass, factory);
	}
}
