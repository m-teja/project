package com.thingstodo.utils

import android.content.Context

object SharedPreferencesUtil {
    const val DELETE_KEY = "THINGS_TO_DO_DELETED_ITEMS"
    const val FILTER_KEY = "THINGS_TO_DO_FILTERED_CATEGORIES"
    private const val DARK_MODE_MAP = "THINGS_TO_DO_DARK_MODE_MAP"
    private const val IS_FIRST_TIME = "THINGS_TO_DO_IS_FIRST_TIME"

    fun isDarkModeMap(context: Context): Boolean {
        val sharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                ?: return true
        return sharedPreferences.getBoolean(DARK_MODE_MAP, true)
    }

    fun setDarkModeMap(context: Context, isDarkMode: Boolean) {
        val sharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) ?: return
        with(sharedPreferences.edit()) {
            putBoolean(DARK_MODE_MAP, isDarkMode)
            apply()
        }
    }

    fun isFirstTime(context: Context): Boolean {
        val sharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                ?: return true
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true)
    }

    fun setFirstTime(context: Context, isFirstTime: Boolean) {
        val sharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) ?: return
        with(sharedPreferences.edit()) {
            putBoolean(IS_FIRST_TIME, isFirstTime)
            apply()
        }
    }
}