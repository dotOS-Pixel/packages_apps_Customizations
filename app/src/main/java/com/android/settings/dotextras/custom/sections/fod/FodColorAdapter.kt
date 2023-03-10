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
package com.android.settings.dotextras.custom.sections.fod

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.android.settings.dotextras.R
import com.dot.ui.utils.ResourceHelper
import com.dot.ui.system.FeatureManager
import com.google.android.material.card.MaterialCardView

class FodColorAdapter(
    private val featureManager: FeatureManager,
    private val items: ArrayList<FodResource>,
) :
    RecyclerView.Adapter<FodColorAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_fod_color,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fodIcon: FodResource = items[position]
        fodIcon.selected =
            featureManager.System().getInt(featureManager.System().FOD_COLOR, 0) == fodIcon.id
        holder.fodIcon.load(ResourceHelper.getDrawable(
            holder.fodIcon.context,
            packageName = holder.fodIcon.context.getString(R.string.systemui_package),
            drawableName = fodIcon.resource
        )) {
            scale(Scale.FIT)
            crossfade(true)
            placeholder(android.R.color.transparent)
        }
        holder.fodLayout.setOnClickListener {
            featureManager.System().setInt(featureManager.System().FOD_COLOR, fodIcon.id)
            select(position)
            updateSelection(fodIcon, holder)
        }
        holder.fodLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val scaleDownX = ObjectAnimator.ofFloat(
                        holder.fodCard,
                        "scaleX", 0.9f
                    )
                    val scaleDownY = ObjectAnimator.ofFloat(
                        holder.fodCard,
                        "scaleY", 0.9f
                    )
                    scaleDownX.duration = 200
                    scaleDownY.duration = 200
                    val scaleDown = AnimatorSet()
                    scaleDown.play(scaleDownX).with(scaleDownY)
                    scaleDown.start()
                }
                MotionEvent.ACTION_UP -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
                MotionEvent.ACTION_CANCEL -> {
                    val scaleDownX2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleX", 1f
                    )
                    val scaleDownY2 = ObjectAnimator.ofFloat(
                        holder.fodCard, "scaleY", 1f
                    )
                    scaleDownX2.duration = 200
                    scaleDownY2.duration = 200
                    val scaleDown2 = AnimatorSet()
                    scaleDown2.play(scaleDownX2).with(scaleDownY2)
                    scaleDown2.start()
                }
            }
            false
        }
        updateSelection(fodIcon, holder)
    }

    private fun updateSelection(fodIcon: FodResource, holder: ViewHolder) {
        val accentColor: Int = ResourceHelper.getAccent(holder.fodLayout.context)
        if (fodIcon.selected) {
            holder.fodCard.strokeColor = accentColor
        } else {
            holder.fodCard.strokeColor = holder.itemView.resources.getColor(
                com.android.internal.R.color.monet_background_secondary_device_default,
                holder.itemView.context.theme)
        }
    }

    private fun select(pos: Int) {
        for (i in items.indices) {
            val selected = items[i].selected
            items[i].selected = pos == i
            if (selected != items[i].selected) notifyItemChanged(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fodCard: MaterialCardView = view.findViewById(R.id.fodCard)
        val fodLayout: FrameLayout = view.findViewById(R.id.fodLayout)
        val fodIcon: ImageView = view.findViewById(R.id.fodIcon)
    }

}