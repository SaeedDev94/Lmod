package io.github.saeeddev94.lmod

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.TextView
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
