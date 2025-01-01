package io.github.saeeddev94.lmod

import android.content.Context
import android.content.Intent
import android.os.Message
import android.provider.Telephony
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebView
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

        loadApp(name = "org.lineageos.jelly") {
            "android.webkit.WebView".toClassOrNull()?.apply {
                method {
                    name = "getSettings"
                }.hook {
                    after {
                        val webSettings = result as android.webkit.WebSettings
                        var userAgent = webSettings.userAgentString
                        if (userAgent.contains("Version")) {
                            userAgent = userAgent.replace("; wv", "")
                            userAgent = userAgent.replace(Regex("\\s*Version/\\S+\\s*"), " ")
                            webSettings.userAgentString = userAgent
                        }
                    }
                }
            }

            "android.webkit.WebChromeClient".toClassOrNull()?.apply {
                method {
                    name = "onPermissionRequest"
                    param(PermissionRequest::class.java)
                }.hook {
                    replaceUnit {
                        val request = args[0] as PermissionRequest
                        val resources = request.resources
                        for (resource in resources) {
                            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resource) {
                                request.grant(resources)
                                return@replaceUnit
                            }
                        }
                    }
                }
            }

            "org.lineageos.jelly.webview.ChromeClient".toClassOrNull()?.apply {
                method {
                    name = "onCreateWindow"
                    param(WebView::class.java, Boolean::class.java, Boolean::class.java, Message::class.java)
                }.hook {
                    replaceAny {
                        val ref = instance::class.java
                        val activity = ref.getDeclaredField("activity")
                        val incognito = ref.getDeclaredField("incognito")
                        activity.isAccessible = true
                        incognito.isAccessible = true

                        val view = args[0] as WebView
                        val isDialog = args[1] as Boolean
                        val isUserGesture = args[2] as Boolean
                        val resultMsg = args[3] as Message

                        ChromeClient(
                            activity.get(instance) as Context,
                            incognito.get(instance) as Boolean,
                        ).onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }
            }

            "org.lineageos.jelly.webview.WebClient".toClassOrNull()?.apply {
                method {
                    name = "onPageFinished"
                }.hook {
                    after {
                        val webView = args[0] as WebView
                        webView.evaluateJavascript(JavaScriptInterface.SYNC_URL_JS, null)
                    }
                }
            }

            "org.lineageos.jelly.webview.WebViewExt".toClassOrNull()?.apply {
                method {
                    name = "init"
                }.hook {
                    after {
                        val webView = instance as WebView
                        val urlBarLayout = args[1] as ViewGroup
                        webView.addJavascriptInterface(JavaScriptInterface(urlBarLayout), JavaScriptInterface.JS_INTERFACE)
                    }
                }
            }
        }

        loadApp(name = "com.android.systemui") {
            "java.util.TimeZone".toClassOrNull()?.apply {
                method {
                    name = "getDefault"
                }.hook {
                    replaceAny {
                        TimeZone.getTimeZone(timeZoneId)
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
        }

        loadApp(name = "com.android.deskclock") {
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
    }

}
