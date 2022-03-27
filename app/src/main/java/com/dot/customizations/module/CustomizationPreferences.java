/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.module;

public interface CustomizationPreferences extends WallpaperPreferences {

    String KEY_CUSTOM_THEME = "themepicker_custom_theme";
    String KEY_VISITED_PREFIX = "themepicker_visited_";
    String KEY_THEMED_ICON_ENABLED = "themepicker_themed_icon_enabled";

    String getSerializedCustomThemes();

    void storeCustomThemes(String serializedCustomThemes);

    boolean getTabVisited(String id);

    void setTabVisited(String id);

    boolean getThemedIconEnabled();

    void setThemedIconEnabled(boolean enabled);
}
