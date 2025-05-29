package io.github.saeeddev94.lmod

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.crossbowffs.remotepreferences.RemotePreferences
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.topjohnwu.superuser.Shell
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    companion object {
        var batteryWidth = 0
        var batteryHeight = 0
    }

    private val timeZoneId: String = "Asia/Tehran"

    override fun onInit() = configs {
        isDebug = false
    }

    override fun onHook() = encase {
        val newTimeZone = {
            "java.util.TimeZone".toClassOrNull()?.apply {
                method {
                    name = "getDefault"
                }.hook {
                    replaceAny {
                        TimeZone.getTimeZone(timeZoneId)
                    }
                }
            }
        }
        val newCalendar = {
            "java.util.Calendar".toClassOrNull()?.apply {
                method {
                    name = "getInstance"
                }.hook {
                    after {
                        val calendar = result as Calendar
                        calendar.timeZone = TimeZone.getTimeZone(timeZoneId)
                    }
                }
            }
        }
        val remotePref = { context: Context ->
            RemotePreferences(context, SharedPref.PKG, SharedPref.NAME, true)
        }

        loadApp(name = "com.android.launcher3") {
            "com.android.launcher3.touch.WorkspaceTouchListener".toClassOrNull()?.apply {
                method {
                    name = "onDoubleTap"
                    superClass()
                }.hook {
                    after {
                        Shell.cmd("input keyevent 223").exec()
                    }
                }
            }
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "com.android.systemui") {
            "com.android.systemui.battery.BatteryMeterView".toClassOrNull()?.apply {
                method {
                    name = "scaleBatteryMeterViews"
                }.hook {
                    after {
                        val sharedPref = remotePref(appContext!!)
                        val scale = sharedPref.getFloat(
                            SharedPref.BATTERY_ICON_SCALE_KEY,
                            SharedPref.BATTERY_ICON_SCALE_DEFAULT
                        )
                        val ref = instance::class.java
                        val icon = ref.getDeclaredField("mBatteryIconView")
                        icon.isAccessible = true
                        val battery = icon.get(instance) as ImageView
                        if (batteryWidth == 0) batteryWidth = battery.layoutParams.width
                        if (batteryHeight == 0) batteryHeight = battery.layoutParams.height
                        val width = Math.round(batteryWidth * scale)
                        val height = Math.round(batteryHeight * scale)
                        battery.layoutParams = LinearLayout.LayoutParams(width, height)
                    }
                }
            }
            "com.android.systemui.statusbar.policy.Clock".toClassOrNull()?.apply {
                method {
                    name = "updateClock"
                }.hook {
                    after {
                        val statusBarClock = instance as TextView
                        val dateTime = ZonedDateTime.now(ZoneId.of(timeZoneId))
                        statusBarClock.text = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    }
                }
            }
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "com.android.deskclock") {
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "com.simplemobiletools.smsmessenger") {
            "com.simplemobiletools.smsmessenger.receivers.SmsReceiver".toClassOrNull()?.apply {
                method {
                    name = "onReceive"
                    param(Context::class.java, Intent::class.java)
                }.hook {
                    after {
                        val context = args[0] as Context
                        val intent = args[1] as Intent
                        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        var text = ""
                        messages.forEach { text += it.messageBody }
                        val otp = OtpExtractor.extract(text) ?: return@after
                        Clipboard.copy(context, otp)
                    }
                }
            }
        }
    }

}
