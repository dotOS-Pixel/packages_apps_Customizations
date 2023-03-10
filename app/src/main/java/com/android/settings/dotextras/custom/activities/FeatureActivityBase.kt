/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import kotlinx.android.synthetic.main.activity_feature.*

class FeatureActivityBase : AppCompatActivity() {

    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature)
        title = intent.extras!!.getString("title")
        val fragment: Fragment =
            Class.forName(intent.extras!!.getString("fragment")!!).newInstance() as Fragment
        val standalone = intent.extras!!.getBoolean("standalone", false)
        appTitle.canGoBack(this)
        setTitle(title)
        //if (standalone) dontScroll()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(if (standalone) R.id.featureCoordinator else R.id.featureContent, fragment, title)
                .commitAllowingStateLoss()
        }
    }

    fun setTitle(title: String?) {
        appTitle.collapsingToolbar.title = title ?: getString(R.string.app_name)
    }


}