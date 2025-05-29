package io.github.saeeddev94.lmod

object SharedPref {
    const val PKG = "io.github.saeeddev94.lmod"
    const val NAME = "lmod_prefs"

    const val BATTERY_ICON_SCALE_KEY = "battery_scale"
    const val BATTERY_ICON_SCALE_DEFAULT = 1.0F

    fun batteryScaleToOption(scale: Float): String {
        val percent = scale - BATTERY_ICON_SCALE_DEFAULT
        val value = percent * 100
        return Math.round(value).toString()
    }

    fun optionToBatteryScale(option: String): Float {
        val percent = option.toInt() / 100F
        return BATTERY_ICON_SCALE_DEFAULT + percent
    }
}
