package com.mitv.investigate

import android.app.Activity

class InstanceHolder {
    companion object {
        private val holder = ArrayList<Activity>()
        fun injectInstance(activity: Activity) {
            holder.add(activity)
        }
    }
}