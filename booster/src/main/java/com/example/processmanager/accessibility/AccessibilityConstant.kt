package com.example.processmanager.accessibility

import android.app.AlertDialog
import java.util.*

object AccessibilityConstant {
    const val SETTING_PACKAGE_NAME = "com.android.settings"
    val APPLICATION_DETAIL_CLASS_NAME = ArrayList<String>()
    val FORCE_STOP_BUTTON_FULL_ID = ArrayList<String>()
    val FORCE_STOP_BUTTON_STRING_ID = ArrayList<String>()
    val CONFIRM_BUTTON_FULL_ID = ArrayList<String>()
    val CONFIRM_BUTTON_STRING_ID = ArrayList<String>()
    val DIALOG_CLASS_NAME = ArrayList<String>()

    init {
        APPLICATION_DETAIL_CLASS_NAME.add("com.miui.appmanager.ApplicationsDetailsActivity")
        APPLICATION_DETAIL_CLASS_NAME.add("com.android.settings.applications.InstalledAppDetailsTop")

        FORCE_STOP_BUTTON_FULL_ID.add("com.android.settings:id/force_stop_button")
        FORCE_STOP_BUTTON_FULL_ID.add("miui:id/v5_icon_menu_bar_primary_item")
        FORCE_STOP_BUTTON_STRING_ID.add("force_stop")
        FORCE_STOP_BUTTON_STRING_ID.add("common_force_stop")
        FORCE_STOP_BUTTON_STRING_ID.add("finish_application")

        CONFIRM_BUTTON_FULL_ID.add("android:id/button1")
        CONFIRM_BUTTON_STRING_ID.add("dlg_ok")
        CONFIRM_BUTTON_STRING_ID.add("ok")

        DIALOG_CLASS_NAME.add(AlertDialog::class.java.name)
        DIALOG_CLASS_NAME.add("miui.app.AlertDialog")
        DIALOG_CLASS_NAME.add("com.htc.widget.HtcAlertDialog")
        DIALOG_CLASS_NAME.add("android.app.AlertDialog")
        DIALOG_CLASS_NAME.add("com.yulong.android.view.dialog.AlertDialog")
        DIALOG_CLASS_NAME.add("com.android.packageinstaller.UninstallerActivity")
    }
}