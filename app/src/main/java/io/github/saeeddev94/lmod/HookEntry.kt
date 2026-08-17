package io.github.saeeddev94.lmod

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.webkit.WebSettings
import android.widget.TextView
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.crossbowffs.remotepreferences.RemotePreferences
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import com.highcapable.kavaref.extension.classOf
import com.highcapable.yukihookapi.hook.log.YLog

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        isDebug = false
    }

    override fun onHook() = encase {
        val remotePref = { context: Context ->
            RemotePreferences(context, SharedPref.PKG, SharedPref.NAME, true)
        }
        val timeZonePref = { context: Context ->
            if (context.packageName.equals("com.android.deskclock")) {
                /**
                 * TODO: Get time zone from app pref
                 * For unknown reason, We can't access the app pref from the clock context!
                 */
                SharedPref.TIME_ZONE_DEFAULT
            } else {
                val sharedPref = remotePref(context)
                sharedPref.getString(
                    SharedPref.TIME_ZONE_KEY,
                    SharedPref.TIME_ZONE_DEFAULT
                )!!
            }
        }
        val newTimeZone = {
            "java.util.TimeZone".toClassOrNull()?.resolve()?.apply {
                firstMethodOrNull {
                    name = "getDefault"
                }?.hook {
                    replaceAny {
                        val timeZoneId = timeZonePref(appContext!!)
                        TimeZone.getTimeZone(timeZoneId)
                    }
                }
            }
        }
        val newCalendar = {
            "java.util.Calendar".toClassOrNull()?.resolve()?.apply {
                firstMethodOrNull {
                    name = "getInstance"
                }?.hook {
                    after {
                        val timeZoneId = timeZonePref(appContext!!)
                        val calendar = result as Calendar
                        calendar.timeZone = TimeZone.getTimeZone(timeZoneId)
                    }
                }
            }
        }

        loadApp(name = "com.android.deskclock") {
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "com.android.launcher3") {
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "com.android.systemui") {
            "com.android.systemui.statusbar.policy.Clock".toClassOrNull()?.resolve()?.apply {
                firstMethodOrNull {
                    name = "updateClock"
                }?.hook {
                    after {
                        val timeZoneId = timeZonePref(appContext!!)
                        val statusBarClock = instance as TextView
                        val dateTime = ZonedDateTime.now(ZoneId.of(timeZoneId))
                        statusBarClock.text = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    }
                }
            }
            newTimeZone()
            newCalendar()
        }

        loadApp(name = "org.fossify.messages") {
            "org.fossify.messages.receivers.SmsReceiver".toClassOrNull()?.resolve()?.apply {
                firstMethodOrNull {
                    name = "onReceive"
                    parameters(classOf<Context>(), classOf<Intent>())
                }?.hook {
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

        loadApp(name = "org.lineageos.jelly") {
            "android.webkit.WebView".toClassOrNull()?.resolve()?.apply {
                firstMethodOrNull {
                    name = "getSettings"
                }?.hook {
                    after {
                        applyDesktopUserAgentData(result as WebSettings)
                    }
                }
            }
        }
    }

    @SuppressLint("RequiresFeature")
    private fun applyDesktopUserAgentData(settings: WebSettings) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) {
            YLog.error(msg = "USER_AGENT_METADATA feature not supported, skipping")
            return
        }
        val metadata = WebSettingsCompat.getUserAgentMetadata(settings)
        val brands = metadata.brandVersionList.filterNot { it.brand == "Android WebView" }
        val builder = UserAgentMetadata.Builder(metadata)
            .setMobile(false)
            .setPlatform("Linux")
            .setPlatformVersion("")
            .setArchitecture("x86")
            .setBitness(64)
            .setModel("")
            .setWow64(false)
            .setBrandVersionList(brands)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA_FORM_FACTORS)) {
            builder.setFormFactors(listOf(UserAgentMetadata.FORM_FACTOR_DESKTOP))
        }
        WebSettingsCompat.setUserAgentMetadata(settings, builder.build())
    }
}
